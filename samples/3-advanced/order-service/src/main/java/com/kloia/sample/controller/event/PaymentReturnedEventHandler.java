package com.kloia.sample.controller.event;

import com.kloia.eventapis.api.EventHandler;
import com.kloia.eventapis.api.EventRepository;
import com.kloia.eventapis.api.ViewQuery;
import com.kloia.eventapis.cassandra.ConcurrentEventException;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.exception.EventPulisherException;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.view.EntityFunctionSpec;
import com.kloia.sample.dto.event.OrderCancelledEvent;
import com.kloia.sample.dto.event.PaymentReturnedEvent;
import com.kloia.sample.model.OrderState;
import com.kloia.sample.model.Orders;
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
public class PaymentReturnedEventHandler implements EventHandler<PaymentReturnedEvent> {
    private final EventRepository eventRepository;
    private final ViewQuery<Orders> orderQuery;

    @Autowired
    public PaymentReturnedEventHandler(EventRepository eventRepository, ViewQuery<Orders> orderQuery) {
        this.eventRepository = eventRepository;
        this.orderQuery = orderQuery;
    }

    @Override
    @KafkaListener(topics = "PaymentReturnedEvent", containerFactory = "eventsKafkaListenerContainerFactory")
    public EventKey execute(PaymentReturnedEvent dto) throws EventStoreException, ConcurrentEventException {
        Orders order = orderQuery.queryEntity(dto.getOrderId());

        if (order.getState() == OrderState.RELEASING_STOCK) {
            OrderCancelledEvent orderCancelledEvent = new OrderCancelledEvent();
            log.info("Payment is processing : " + dto);
            return eventRepository.recordAndPublish(order, orderCancelledEvent);
        } else
            throw new EventStoreException("Order state is not valid for this Operation: " + dto);
    }

    @Component
    public static class PaymentProcessSpec extends EntityFunctionSpec<Orders, OrderCancelledEvent> {
        public PaymentProcessSpec() {
            super((order, event) -> {
                order.setState(OrderState.CANCELLED);
                return order;
            });
        }
    }
}
