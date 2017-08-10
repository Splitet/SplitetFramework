package com.kloia.sample.commands;

import com.kloia.eventapis.api.Command;
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

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
@Slf4j
@Controller
public class CreateStockCommand implements Command<Stock, CreateStockCommandDto> {
    private final static String name = "CREATE_STOCK";
    private final static String CREATED = "CREATED";
    private final EventRepository<Stock> eventRepository;
    private final Query<Stock> orderQuery;



    @Autowired
    public CreateStockCommand(EventRepository<Stock> eventRepository, Query<Stock> orderQuery) {
        this.eventRepository = eventRepository;
        this.orderQuery = orderQuery;
    }

    @Override
    public EventKey execute(CreateStockCommandDto dto) throws Exception {
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
