package com.kloia.sample.controller.event;

import com.kloia.eventapis.api.EventHandler;
import com.kloia.eventapis.api.EventRepository;
import com.kloia.eventapis.api.ViewQuery;
import com.kloia.eventapis.cassandra.ConcurrencyResolver;
import com.kloia.eventapis.cassandra.ConcurrentEventException;
import com.kloia.eventapis.cassandra.EntityEvent;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.view.Entity;
import com.kloia.eventapis.view.EntityFunctionSpec;
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
        StockReservedEvent stockReservedEvent = new StockReservedEvent();
        BeanUtils.copyProperties(dto, stockReservedEvent);
        stockReservedEvent.setOrderId(dto.getSender().getEntityId());
        try {
            return eventRepository.recordAndPublish(new EventKey(stock.getId(), stock.getVersion() - 1), stockReservedEvent, entityEvent -> new StockConcurrencyResolver(stockQuery,dto));
        } catch (ConcurrentEventException e) {
            StockNotEnoughEvent stockNotEnoughEvent = new StockNotEnoughEvent();
            BeanUtils.copyProperties(dto, stockNotEnoughEvent);
            return eventRepository.recordAndPublish(stock,stockNotEnoughEvent);
        }catch (Throwable e) {
            log.error(e.getMessage(),e);
            return stock.getEventKey();
        }

/*        if (stock.getRemainingStock() < dto.getNumberOfItemsSold()) {
            StockNotEnoughEvent stockNotEnoughEvent = new StockNotEnoughEvent();
            BeanUtils.copyProperties(dto, stockNotEnoughEvent);
            return eventRepository.recordAndPublish(stockNotEnoughEvent);
        } else {
            StockReservedEvent stockReservedEvent = new StockReservedEvent();
            BeanUtils.copyProperties(dto, stockReservedEvent);
            stockReservedEvent.setOrderId(dto.getSender().getEntityId());
            return eventRepository.recordAndPublish(new EventKey(stock.getId(),stock.getVersion()-1),stockReservedEvent);
        }*/
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


    private static class StockConcurrencyResolver implements ConcurrencyResolver {
        private int maxTry = 3;
        private int currentTry = 0;

        public StockConcurrencyResolver(ViewQuery<Stock> stockQuery, ReserveStockEvent reserveStockEvent) {
            this.stockQuery = stockQuery;
            this.reserveStockEvent = reserveStockEvent;
        }

        ViewQuery<Stock> stockQuery;
        ReserveStockEvent reserveStockEvent;

        @Override
        public boolean tryMore() {
            return maxTry > currentTry++;
        }

        @Override
        public boolean hasMore() {
            return maxTry > currentTry;
        }

        @Override
        public EntityEvent calculateNext(EntityEvent entityEvent, int lastVersion) throws ConcurrentEventException, EventStoreException {
            Stock stock = stockQuery.queryEntity(entityEvent.getEventKey().getEntityId());
            if (stock.getRemainingStock() < reserveStockEvent.getNumberOfItemsSold()){
                throw new ConcurrentEventException("Out Of Stock Event");
            }else{
                entityEvent.setEventKey(new EventKey(entityEvent.getEventKey().getEntityId(),lastVersion +1));
                return entityEvent;
            }
        }
    }
}
