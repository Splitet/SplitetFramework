package io.splitet.sample.commands;

import io.splitet.core.api.Command;
import io.splitet.core.api.CommandHandler;
import io.splitet.core.api.EventRepository;
import io.splitet.core.api.RollbackCommandSpec;
import io.splitet.core.api.RollbackSpec;
import io.splitet.core.api.ViewQuery;
import io.splitet.core.common.EventKey;
import io.splitet.core.view.EntityFunctionSpec;
import io.splitet.sample.dto.command.AddStockCommandDto;
import io.splitet.sample.dto.event.StockAddedEvent;
import io.splitet.sample.model.Stock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
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
    @Command()
    public EventKey execute(@PathVariable String stockId, @RequestBody AddStockCommandDto dto) throws Exception {
        dto.setStockId(stockId);
        Stock stock = stockQuery.queryEntity(dto.getStockId());

        if (dto.getStockToAdd() > 1000000) {
            throw new IllegalArgumentException("Invalid Stock to Add");
        }

        StockAddedEvent stockAddedEvent = new StockAddedEvent(dto.getStockToAdd());
        return eventRepository.recordAndPublish(stock.getEventKey(), stockAddedEvent);
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
    public static class AddStockCommandRollbackSpec implements RollbackCommandSpec<AddStockCommandHandler> {

        public void rollback(String stockId, AddStockCommandDto event) {
            log.warn("Rolling back AddStockCommandDto for: " + event.toString());
        }
    }

}
