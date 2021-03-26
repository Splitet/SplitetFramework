package io.splitet.sample.controller.event;

import io.splitet.core.api.EventHandler;
import io.splitet.core.api.EventRepository;
import io.splitet.core.api.ViewQuery;
import io.splitet.core.cassandra.ConcurrentEventException;
import io.splitet.core.common.EventKey;
import io.splitet.core.exception.EventStoreException;
import io.splitet.core.view.EntityFunctionSpec;
import io.splitet.sample.dto.event.OrderPaidEvent;
import io.splitet.sample.dto.event.PaymentSuccessEvent;
import io.splitet.sample.model.OrderState;
import io.splitet.sample.model.Orders;
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
public class PaymentSuccessEventHandler implements EventHandler<PaymentSuccessEvent> {

    private final EventRepository eventRepository;
    private final ViewQuery<Orders> orderQuery;

    @Autowired
    public PaymentSuccessEventHandler(EventRepository eventRepository, ViewQuery<Orders> orderQuery) {
        this.eventRepository = eventRepository;
        this.orderQuery = orderQuery;
    }

    @Override
    @KafkaListener(topics = "PaymentSuccessEvent", containerFactory = "eventsKafkaListenerContainerFactory")
    public EventKey execute(PaymentSuccessEvent dto) throws EventStoreException, ConcurrentEventException {
        Orders order = orderQuery.queryEntity(dto.getOrderId());

        if (order.getState() == OrderState.PAYMENT_READY) {
            log.info("Payment is processing : " + dto);
            return eventRepository.recordAndPublish(order.getEventKey(), new OrderPaidEvent(dto.getSender().getEntityId()));
        } else
            throw new EventStoreException("Order state is not valid for this Operation: " + dto);
    }

    @Component
    public static class OrderPaidSpec extends EntityFunctionSpec<Orders, OrderPaidEvent> {
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
