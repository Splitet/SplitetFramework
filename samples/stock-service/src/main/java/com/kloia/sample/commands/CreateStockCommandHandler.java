package com.kloia.sample.commands;

import com.kloia.eventapis.api.CommandHandler;
import com.kloia.eventapis.view.EntityFunctionSpec;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.api.EventRepository;
import com.kloia.eventapis.api.Query;
import com.kloia.sample.dto.command.CreateStockCommandDto;
import com.kloia.sample.dto.event.StockCreatedEvent;
import com.kloia.sample.model.Stock;
import com.kloia.sample.model.StockState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
@Slf4j
@RestController
public class CreateStockCommandHandler implements CommandHandler<Stock, CreateStockCommandDto> {
    private final static String name = "CREATE_STOCK";
    private final static String CREATED = "CREATED";
    private final EventRepository<Stock> eventRepository;
    private final Query<Stock> orderQuery;



    @Autowired
    public CreateStockCommandHandler(EventRepository<Stock> eventRepository, Query<Stock> orderQuery) {
        this.eventRepository = eventRepository;
        this.orderQuery = orderQuery;
    }

    @Override
    @RequestMapping(value = "/stock/v1/create", method = RequestMethod.POST)
    public EventKey execute(@RequestBody CreateStockCommandDto dto) throws Exception {
        StockCreatedEvent stockCreatedEvent = new StockCreatedEvent();
        BeanUtils.copyProperties(dto,stockCreatedEvent);
        return eventRepository.recordAndPublish(stockCreatedEvent);
    }

    @Component
    public static class CreateStockSpec extends EntityFunctionSpec<Stock, StockCreatedEvent> {
        public CreateStockSpec() {
            super((stock, event) -> {
                StockCreatedEvent eventData = event.getEventData();
                stock.setRemainingStock(eventData.getRemainingStock());
                stock.setStockName(eventData.getStockName());
                stock.setState(StockState.INUSE);
                return stock;
            });
        }
    }


}
