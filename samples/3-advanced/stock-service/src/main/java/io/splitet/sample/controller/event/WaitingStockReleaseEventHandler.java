package io.splitet.sample.controller.event;

import io.splitet.core.api.EventHandler;
import io.splitet.core.api.EventRepository;
import io.splitet.core.api.ViewQuery;
import io.splitet.core.cassandra.ConcurrencyResolver;
import io.splitet.core.common.EventKey;
import io.splitet.core.view.EntityFunctionSpec;
import io.splitet.sample.dto.StockNotEnoughException;
import io.splitet.sample.dto.event.StockReleasedEvent;
import io.splitet.sample.dto.event.StockReservedEvent;
import io.splitet.sample.dto.event.WaitingStockReleaseEvent;
import io.splitet.sample.model.Stock;
import io.splitet.sample.model.StockState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
@Slf4j
@Controller
public class WaitingStockReleaseEventHandler implements EventHandler<WaitingStockReleaseEvent> {
    private final EventRepository eventRepository;
    private final ViewQuery<Stock> stockQuery;

    @Autowired
    public WaitingStockReleaseEventHandler(EventRepository eventRepository, ViewQuery<Stock> stockQuery) {
        this.eventRepository = eventRepository;
        this.stockQuery = stockQuery;
    }

    @Override
    @KafkaListener(topics = "WaitingStockReleaseEvent", containerFactory = "eventsKafkaListenerContainerFactory")
    public EventKey execute(WaitingStockReleaseEvent dto) throws Exception {
        Stock stock = stockQuery.queryEntity(dto.getStockId());
        StockReservedEvent stockReservedEvent = stockQuery.queryEventData(dto.getStockId(), dto.getReservedStockVersion());

        return eventRepository.recordAndPublish(stock,
                new StockReleasedEvent(dto.getSender().getEntityId(), stockReservedEvent.getNumberOfItemsSold()),
                entityEvent -> new StockConcurrencyResolver());
    }

    @Component
    public static class StockReleasedSpec extends EntityFunctionSpec<Stock, StockReleasedEvent> {
        public StockReleasedSpec() {
            super((stock, event) -> {
                StockReleasedEvent eventData = event.getEventData();
                stock.setRemainingStock(stock.getRemainingStock() + eventData.getNumberOfItemsReleased());
                if (stock.getState() == StockState.OUT)
                    stock.setState(StockState.INUSE);
                return stock;
            });
        }
    }

    private static class StockConcurrencyResolver implements ConcurrencyResolver<StockNotEnoughException> {
        private int maxTry = 100;
        private int currentTry = 0;

        @Override
        public void tryMore() throws StockNotEnoughException {
            if (maxTry <= currentTry++)
                throw new StockNotEnoughException("Cannot allocate stock in Max Try: " + maxTry);
        }

        @Override
        public EventKey calculateNext(EventKey eventKey, int lastVersion) {
            return new EventKey(eventKey.getEntityId(), lastVersion + 1);
        }
    }
}
