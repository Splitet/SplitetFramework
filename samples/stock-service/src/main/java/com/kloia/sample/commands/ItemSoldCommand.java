package com.kloia.sample.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.api.impl.OperationRepository;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.evented.*;
import com.kloia.sample.dto.ItemSoldAggDTO;
import com.kloia.sample.dto.Stock;
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
public class ItemSoldCommand extends CommandSpec<Stock, ItemSoldAggDTO> {
    private final static String name = "ITEM_SOLD";
    private OperationRepository operationRepository;
    private final static String CREATED = "CREATED";


    @Autowired
    public ItemSoldCommand(ObjectMapper objectMapper, OperationRepository operationRepository, IEventRepository<Stock> eventRepository) {
        super(name,objectMapper,eventRepository, (stock, event) -> {
            try {
                if(event.getStatus().equals(CREATED)){
                    ItemSoldAggDTO itemSoldAggDTO = objectMapper.readerFor(ItemSoldAggDTO.class).readValue(event.getEventData());
                    stock.setRemainingStock(stock.getRemainingStock()-itemSoldAggDTO.getNumberOfItemsSold());
                }
                return stock;
            } catch (Exception e) {
                log.error("Error while applying Aggregate:" + event + " Exception:" + e.getMessage(), e);
                throw new EventStoreException("Error while applying Aggregate:" + event + " Exception:" + e.getMessage(), e);
            }
        });
        this.operationRepository = operationRepository;
    }

    @Override
    public void processCommand(ItemSoldAggDTO itemSoldAggDTO) throws EventStoreException {
        Stock stock = eventRepository.queryEntity(itemSoldAggDTO.getStockId());
        if (stock != null && stock.getRemainingStock() >= itemSoldAggDTO.getNumberOfItemsSold()) {
            try {
                Map.Entry<UUID, Operation> context = operationRepository.getContext();
                EntityEvent entityEvent = createEvent(new EventKey(itemSoldAggDTO.getStockId(),stock.getVersion() + 1),CREATED, itemSoldAggDTO,context.getKey());
                getEventRepository().recordAggregateEvent(entityEvent);
            } catch (Exception e) {
                throw new EventStoreException("Error while processing Command:" + itemSoldAggDTO + " Exception: "+e.getMessage(),e);
            }
        } else
            throw new EventStoreException("Stock amount is not valid for this Operation: " + itemSoldAggDTO);
    }


}
