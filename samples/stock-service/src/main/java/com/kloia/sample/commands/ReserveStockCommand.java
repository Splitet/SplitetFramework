package com.kloia.sample.commands;

import com.kloia.eventapis.pojos.EventKey;
import com.kloia.evented.Command;
import com.kloia.evented.EntityFunctionSpec;
import com.kloia.evented.EventRepository;
import com.kloia.evented.Query;
import com.kloia.sample.dto.event.ReserveStockEvent;
import com.kloia.sample.dto.event.StockNotEnoughEvent;
import com.kloia.sample.dto.event.StockReservedEvent;
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
public class ReserveStockCommand implements Command<Stock, ReserveStockEvent> {
    private final static String name = "CREATE_STOCK";
    private final static String CREATED = "CREATED";
    private final EventRepository<Stock> eventRepository;
    private final Query<Stock> stockQuery;

    @Autowired
    public ReserveStockCommand(EventRepository<Stock> eventRepository, Query<Stock> stockQuery) {
        this.eventRepository = eventRepository;
        this.stockQuery = stockQuery;
    }

    @Override
    public EventKey execute(ReserveStockEvent dto) throws Exception {
        Stock stock = stockQuery.queryEntity(dto.getStockId());
        if (stock.getRemainingStock() < dto.getNumberOfItemsSold()) {
            StockNotEnoughEvent stockNotEnoughEvent = new StockNotEnoughEvent();
            BeanUtils.copyProperties(dto, stockNotEnoughEvent);
            return eventRepository.recordAndPublish(stockNotEnoughEvent);
        } else {
            StockReservedEvent stockReservedEvent = new StockReservedEvent();
            BeanUtils.copyProperties(dto, stockReservedEvent);
            stockReservedEvent.setOrderId(dto.getSender().getEntityId());
            return eventRepository.recordAndPublish(stock,stockReservedEvent);
        }
    }

    @Component
    public static class ReserveStockSpec extends EntityFunctionSpec<Stock, StockReservedEvent> {
        public ReserveStockSpec() {
            super((stock, event) -> {
                StockReservedEvent eventData = event.getEventData();
                stock.setRemainingStock(stock.getRemainingStock() - eventData.getNumberOfItemsSold());
                return stock;
            });
        }
    }

    @Component
    public static class StockNotEnoughSpec extends EntityFunctionSpec<Stock, StockNotEnoughEvent> {
        public StockNotEnoughSpec() {
            super((stock, event) -> {
                StockNotEnoughEvent eventData = event.getEventData();
                stock.setState(StockState.OUT);
                return stock;
            });
        }
    }


}
