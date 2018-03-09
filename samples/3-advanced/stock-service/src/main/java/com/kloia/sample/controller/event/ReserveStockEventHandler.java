package com.kloia.sample.controller.event;

import com.kloia.eventapis.api.EventHandler;
import com.kloia.eventapis.api.EventRepository;
import com.kloia.eventapis.api.ViewQuery;
import com.kloia.eventapis.cassandra.ConcurrencyResolver;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.view.EntityFunctionSpec;
import com.kloia.sample.dto.StockNotEnoughException;
import com.kloia.sample.dto.event.ReserveStockEvent;
import com.kloia.sample.dto.event.StockNotEnoughEvent;
import com.kloia.sample.dto.event.StockReservedEvent;
import com.kloia.sample.model.Stock;
import com.kloia.sample.model.StockState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
@Slf4j
@Controller
public class ReserveStockEventHandler implements EventHandler<ReserveStockEvent> {
    private final EventRepository eventRepository;
    private final ViewQuery<Stock> stockQuery;

    @Autowired
    public ReserveStockEventHandler(EventRepository eventRepository, ViewQuery<Stock> stockQuery) {
        this.eventRepository = eventRepository;
        this.stockQuery = stockQuery;
    }

    @Override
    @KafkaListener(topics = "ReserveStockEvent", containerFactory = "eventsKafkaListenerContainerFactory")
    public EventKey execute(ReserveStockEvent dto) throws Exception {
        Stock stock = stockQuery.queryEntity(dto.getStockId());
        if (stock.getRemainingStock() >= dto.getNumberOfItemsSold()) {
            StockReservedEvent stockReservedEvent = new StockReservedEvent();
            BeanUtils.copyProperties(dto, stockReservedEvent);
            stockReservedEvent.setOrderId(dto.getSender().getEntityId());
            try {
                return eventRepository.recordAndPublish(stock, stockReservedEvent, entityEvent -> new StockConcurrencyResolver(stockQuery, dto));
            } catch (StockNotEnoughException e) {
                return recordStockNotEnough(dto, stock);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return stock.getEventKey();
            }
        } else
            return recordStockNotEnough(dto, stock);
    }

    private EventKey recordStockNotEnough(ReserveStockEvent dto, Stock stock) throws EventStoreException, com.kloia.eventapis.cassandra.ConcurrentEventException {
        StockNotEnoughEvent stockNotEnoughEvent = new StockNotEnoughEvent();
        BeanUtils.copyProperties(dto, stockNotEnoughEvent);
        return eventRepository.recordAndPublish(stock, stockNotEnoughEvent);
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

    private static class StockConcurrencyResolver implements ConcurrencyResolver<StockNotEnoughException> {
        ViewQuery<Stock> stockQuery;
        ReserveStockEvent reserveStockEvent;
        private int maxTry = 3;
        private int currentTry = 0;
        public StockConcurrencyResolver(ViewQuery<Stock> stockQuery, ReserveStockEvent reserveStockEvent) {
            this.stockQuery = stockQuery;
            this.reserveStockEvent = reserveStockEvent;
        }

        @Override
        public void tryMore() throws StockNotEnoughException {
            if (maxTry <= currentTry++)
                throw new StockNotEnoughException("Cannot allocate stock in Max Try: " + maxTry);
        }


        @Override
        public EventKey calculateNext(EventKey eventKey, int lastVersion) throws StockNotEnoughException, EventStoreException {
            Stock stock = stockQuery.queryEntity(eventKey.getEntityId());
            if (stock.getRemainingStock() < reserveStockEvent.getNumberOfItemsSold()) {
                throw new StockNotEnoughException("Out Of Stock Event");
            } else {
                return new EventKey(eventKey.getEntityId(), stock.getVersion() + 1);
            }
        }
    }
}
