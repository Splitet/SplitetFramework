package com.kloia.sample.commands;

import com.kloia.eventapis.api.CommandHandler;
import com.kloia.eventapis.api.EventRepository;
import com.kloia.eventapis.api.RollbackSpec;
import com.kloia.eventapis.api.ViewQuery;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.view.EntityFunctionSpec;
import com.kloia.sample.dto.command.ProcessOrderCommandDto;
import com.kloia.sample.dto.event.WaitingStockReleaseEvent;
import com.kloia.sample.model.OrderState;
import com.kloia.sample.model.Orders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
@Slf4j
@RestController
public class CancelOrderCommand implements CommandHandler<ProcessOrderCommandDto> {
    private final EventRepository eventRepository;
    private final ViewQuery<Orders> orderQuery;

    @Autowired
    public CancelOrderCommand(EventRepository eventRepository, ViewQuery<Orders> orderQuery) {
        this.eventRepository = eventRepository;
        this.orderQuery = orderQuery;
    }

    @RequestMapping(value = "/order/{orderId}/cancel", method = RequestMethod.POST)
    public EventKey execute(@PathVariable("orderId") String orderId, @RequestBody @Valid ProcessOrderCommandDto dto) throws Exception {
        dto.setOrderId(orderId);
        return this.execute(dto);
    }

    @Override
    public EventKey execute(@RequestBody ProcessOrderCommandDto dto) throws Exception {
        Orders order = orderQuery.queryEntity(dto.getOrderId());

        if (order.getState() == OrderState.PAID) {
            WaitingStockReleaseEvent waitingStockReleaseEvent = new WaitingStockReleaseEvent(order.getStockId(), order.getReservedStockVersion());
            return eventRepository.recordAndPublish(order, waitingStockReleaseEvent);
        } else
            throw new EventStoreException("Order state is not valid for this Operation: " + dto);
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
}
