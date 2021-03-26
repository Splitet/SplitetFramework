package io.splitet.sample.commands;

import io.splitet.core.api.Command;
import io.splitet.core.api.CommandHandler;
import io.splitet.core.api.EventRepository;
import io.splitet.core.api.RollbackCommandSpec;
import io.splitet.core.api.RollbackSpec;
import io.splitet.core.api.ViewQuery;
import io.splitet.core.common.EventKey;
import io.splitet.core.exception.EventStoreException;
import io.splitet.core.view.EntityFunctionSpec;
import io.splitet.sample.dto.event.WaitingStockReleaseEvent;
import io.splitet.sample.model.OrderState;
import io.splitet.sample.model.Orders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
@Slf4j
@RestController
public class CancelOrderCommand implements CommandHandler {

    private final EventRepository eventRepository;
    private final ViewQuery<Orders> orderQuery;

    @Autowired
    public CancelOrderCommand(EventRepository eventRepository, ViewQuery<Orders> orderQuery) {
        this.eventRepository = eventRepository;
        this.orderQuery = orderQuery;
    }

    @RequestMapping(value = "/order/{orderId}/cancel", method = RequestMethod.POST)
    @Command
    public EventKey execute(@PathVariable("orderId") String orderId) throws Exception {
        Orders order = orderQuery.queryEntity(orderId);

        if (order.getState() == OrderState.PAID) {
            WaitingStockReleaseEvent waitingStockReleaseEvent = new WaitingStockReleaseEvent(order.getStockId(), order.getReservedStockVersion());
            return eventRepository.recordAndPublish(order, waitingStockReleaseEvent);
        } else
            throw new EventStoreException("Order state is not valid for this Operation: " + orderId);
    }

    @Component
    public static class ProcessOrderSpec extends EntityFunctionSpec<Orders, WaitingStockReleaseEvent> {
        public ProcessOrderSpec() {
            super((order, event) -> {
                WaitingStockReleaseEvent eventData = event.getEventData();
                order.setState(OrderState.RELEASING_STOCK);
                return order;
            });
        }
    }

    @Component
    public static class WaitingStockReleaseRollback implements RollbackSpec<WaitingStockReleaseEvent> {
        @Override
        public void rollback(WaitingStockReleaseEvent event) {
            log.warn("Rollback WaitingStockReleaseEvent for :" + event);
        }
    }

    @Component
    public static class CancelOrderCommandRollback implements RollbackCommandSpec<CancelOrderCommand> {
        public void rollback(String orderId) {
            log.warn("Rollback CancelOrderCommand for :" + orderId);

        }
    }
}
