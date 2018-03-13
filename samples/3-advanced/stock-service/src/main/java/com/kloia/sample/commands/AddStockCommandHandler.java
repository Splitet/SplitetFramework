package com.kloia.sample.commands;

import com.kloia.eventapis.api.Command;
import com.kloia.eventapis.api.CommandHandler;
import com.kloia.eventapis.api.EventRepository;
import com.kloia.eventapis.api.RollbackSpec;
import com.kloia.eventapis.api.ViewQuery;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.view.EntityFunctionSpec;
import com.kloia.sample.dto.command.AddStockCommandDto;
import com.kloia.sample.dto.event.StockAddedEvent;
import com.kloia.sample.model.Stock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
@Slf4j
@RestController
public class AddStockCommandHandler implements CommandHandler {
    private final EventRepository eventRepository;
    private final ViewQuery<Stock> stockQuery;


    @Autowired
    public AddStockCommandHandler(EventRepository eventRepository, ViewQuery<Stock> stockQuery) {
        this.eventRepository = eventRepository;
        this.stockQuery = stockQuery;
    }


    @RequestMapping(value = "/stock/{stockId}/add", method = RequestMethod.POST)
    @Command
    public EventKey execute(String stockId, @RequestBody AddStockCommandDto dto) throws Exception {
        dto.setStockId(stockId);
        Stock stock = stockQuery.queryEntity(dto.getStockId());

        if (dto.getStockToAdd() > 1000000)
            throw new IllegalArgumentException("Invalid Stock to Add");

        return eventRepository.recordAndPublish(stock.getEventKey(), new StockAddedEvent(dto.getStockToAdd()));
    }

    @Component
    public static class AddStockSpec extends EntityFunctionSpec<Stock, StockAddedEvent> {
        public AddStockSpec() {
            super((stock, event) -> {
                StockAddedEvent eventData = event.getEventData();
                stock.setRemainingStock(stock.getRemainingStock() + eventData.getAddedStock());
                return stock;
            });
        }
    }

    @Component
    public static class AddStockRollbackSpec implements RollbackSpec<StockAddedEvent> {

        @Override
        public void rollback(StockAddedEvent event) {
            log.warn("Rolling back StockAddedEvent for: " + event.toString());
        }
    }

    @Component
    public static class AddStockCommandRollbackSpec implements RollbackSpec<AddStockCommandDto> {

        @Override
        public void rollback(AddStockCommandDto event) {
            log.warn("Rolling back AddStockCommandDto for: " + event.toString());
        }
    }


}
