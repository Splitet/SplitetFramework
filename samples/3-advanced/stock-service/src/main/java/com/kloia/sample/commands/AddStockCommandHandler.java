package com.kloia.sample.commands;

import com.kloia.eventapis.api.CommandHandler;
import com.kloia.eventapis.api.EventRepository;
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
public class AddStockCommandHandler implements CommandHandler<AddStockCommandDto> {
    private final EventRepository eventRepository;
    private final ViewQuery<Stock> stockQuery;


    @Autowired
    public AddStockCommandHandler(EventRepository eventRepository, ViewQuery<Stock> stockQuery) {
        this.eventRepository = eventRepository;
        this.stockQuery = stockQuery;
    }

    @RequestMapping(value = "/stock/{stockId}/add", method = RequestMethod.POST)
    public EventKey execute(String stockId, @RequestBody AddStockCommandDto dto) throws Exception {
        dto.setStockId(stockId);
        return execute(dto);
    }

    public EventKey execute(@RequestBody AddStockCommandDto dto) throws Exception {
        Stock stock = stockQuery.queryEntity(dto.getStockId());

        return eventRepository.recordAndPublish(stock.getEventKey(), new StockAddedEvent(dto.getStockToAdd()));
    }

    @Component
    public static class CreateStockSpec extends EntityFunctionSpec<Stock, StockAddedEvent> {
        public CreateStockSpec() {
            super((stock, event) -> {
                StockAddedEvent eventData = event.getEventData();
                stock.setRemainingStock(stock.getRemainingStock() + eventData.getAddedStock());
                return stock;
            });
        }
    }


}
