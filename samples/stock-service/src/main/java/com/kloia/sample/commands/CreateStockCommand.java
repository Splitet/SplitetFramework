package com.kloia.sample.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.api.impl.OperationRepository;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.evented.*;
import com.kloia.sample.dto.Stock;
import com.kloia.sample.dto.StockCreateAggDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
@Slf4j
@Controller
public class CreateStockCommand extends CommandSpec<Stock, StockCreateAggDTO> {
    private final static String name = "CREATE_STOCK";
    private OperationRepository operationRepository;
    private final static String CREATED = "CREATED";


    @Autowired
    public CreateStockCommand(ObjectMapper objectMapper, OperationRepository operationRepository, IEventRepository<Stock> eventRepository) {
        super(name,objectMapper,eventRepository, (stock, event) -> {
            try {
                StockCreateAggDTO stockCreateAggDTO = objectMapper.readerFor(StockCreateAggDTO.class).readValue(event.getEventData());
                stock = new Stock();
                stock.setStockId(stockCreateAggDTO.getStockId());
                stock.setRemainingStock(stockCreateAggDTO.getRemainingStock());
                stock.setStockName(stockCreateAggDTO.getStockName());
                stock.setState("CREATED");
                return stock;
            } catch (Exception e) {
                log.error("Error while applying Aggregate:" + event + " Exception:" + e.getMessage(), e);
                throw new EventStoreException("Error while applying Aggregate:" + event + " Exception:" + e.getMessage(), e);
            }
        });

        this.operationRepository = operationRepository;
    }

    @Override
    public void processCommand(StockCreateAggDTO stockCreateAggDTO) throws EventStoreException {
        try {
            Map.Entry<UUID, Operation> context = operationRepository.getContext();
            EntityEvent entityEvent = createEvent(new EventKey(stockCreateAggDTO.getStockId(),0),"CREATED", stockCreateAggDTO,context.getKey());
            getEventRepository().recordAggregateEvent(entityEvent);
        } catch (Exception e) {
            throw new EventStoreException("Error while processing Command:" + stockCreateAggDTO + " Exception: "+e.getMessage(),e);
        }
    }


}
