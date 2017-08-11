package com.kloia.sample.controller.event;

import com.kloia.eventapis.api.CommandHandler;
import com.kloia.eventapis.api.EventHandler;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.view.EntityFunctionSpec;
import com.kloia.eventapis.exception.EventPulisherException;
import com.kloia.eventapis.api.EventRepository;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.api.Query;
import com.kloia.sample.dto.event.OrderPaidEvent;
import com.kloia.sample.dto.event.PaymentSuccessEvent;
import com.kloia.sample.model.Order;
import com.kloia.sample.model.OrderState;
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
public class PaymentSuccessEventHandler implements EventHandler<Order, PaymentSuccessEvent> {
    private final static String name = "PROCESS_ORDER";
    private final static String CREATED = "CREATED";
    private final EventRepository<Order> eventRepository;
    private final Query<Order> orderQuery;

    @Autowired
    public PaymentSuccessEventHandler(EventRepository<Order> eventRepository, Query<Order> orderQuery) {
        this.eventRepository = eventRepository;
        this.orderQuery = orderQuery;
    }

    @Override
    @KafkaListener(topics = "PaymentSuccessEvent", containerFactory = "eventsKafkaListenerContainerFactory")
    public EventKey execute(PaymentSuccessEvent dto) throws EventStoreException, EventPulisherException {
        Order order = orderQuery.queryEntity(dto.getOrderId());

        if (order.getState() == OrderState.PAYMENT_READY) {
            log.info("Payment is processing : " + dto);
            return eventRepository.recordAndPublish(order,new OrderPaidEvent(dto.getPaymentId()));
        } else
            throw new EventStoreException("Order state is not valid for this Operation: " + dto);
    }

    @Component
    public static class OrderPaidSpec extends EntityFunctionSpec<Order, OrderPaidEvent> {
        public OrderPaidSpec() {
            super((order, event) -> {
                OrderPaidEvent eventData = event.getEventData();
                order.setPaymentId(eventData.getPaymentId());
                order.setState(OrderState.PAID);
                return order;
            });
        }
    }
}
