package io.splitet.sample.controller.event;

import io.splitet.core.api.EventHandler;
import io.splitet.core.api.EventRepository;
import io.splitet.core.api.ViewQuery;
import io.splitet.core.cassandra.ConcurrentEventException;
import io.splitet.core.cassandra.ConcurrentEventResolver;
import io.splitet.core.common.EventKey;
import io.splitet.core.exception.EventStoreException;
import io.splitet.core.view.EntityFunctionSpec;
import io.splitet.sample.dto.StockNotEnoughException;
import io.splitet.sample.dto.event.ReserveStockEvent;
import io.splitet.sample.dto.event.StockNotEnoughEvent;
import io.splitet.sample.dto.event.StockReservedEvent;
import io.splitet.sample.model.Stock;
import io.splitet.sample.model.StockState;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
                return eventRepository.recordAndPublish(new EventKey(stock.getId(), stock.getVersion() - 1), stockReservedEvent, () -> new StockConcurrencyResolver(stockQuery, dto));
            } catch (StockNotEnoughException e) {
                return recordStockNotEnough(dto, stock);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return stock.getEventKey();
            }
        } else
            return recordStockNotEnough(dto, stock);
    }

    private EventKey recordStockNotEnough(ReserveStockEvent dto, Stock stock) throws EventStoreException, ConcurrentEventException {
        StockNotEnoughEvent stockNotEnoughEvent = new StockNotEnoughEvent();
        BeanUtils.copyProperties(dto, stockNotEnoughEvent);
        return eventRepository.recordAndPublish(stock, stockNotEnoughEvent);
    }

    @Component
    public static class StockReservedSpec extends EntityFunctionSpec<Stock, StockReservedEvent> {
        public StockReservedSpec() {
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

    private static class StockConcurrencyResolver implements ConcurrentEventResolver<StockReservedEvent, StockNotEnoughException> {

        private ViewQuery<Stock> stockQuery;
        private ReserveStockEvent reserveStockEvent;

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
        public Pair<EventKey, StockReservedEvent> calculateNext(
                StockReservedEvent failedEvent, EventKey failedEventKey, int lastVersion
        ) throws StockNotEnoughException, EventStoreException {
            Stock stock = stockQuery.queryEntity(failedEventKey.getEntityId());
            if (stock.getRemainingStock() < reserveStockEvent.getNumberOfItemsSold()) {
                throw new StockNotEnoughException("Out Of Stock Event");
            } else {
                return new ImmutablePair<>(new EventKey(failedEventKey.getEntityId(), stock.getVersion() + 1), failedEvent);
            }
        }

        /*
        @Override
        public EventKey calculateNext(EventKey eventKey, int lastVersion) throws StockNotEnoughException, EventStoreException {
            Stock stock = stockQuery.queryEntity(eventKey.getEntityId());
            if (stock.getRemainingStock() < reserveStockEvent.getNumberOfItemsSold()) {
                throw new StockNotEnoughException("Out Of Stock Event");
            } else {
                return new EventKey(eventKey.getEntityId(), stock.getVersion() + 1);
            }
        }
        */
    }
}
