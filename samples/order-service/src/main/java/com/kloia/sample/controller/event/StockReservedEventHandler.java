package com.kloia.sample.controller.event;

import com.kloia.eventapis.api.EventHandler;
import com.kloia.eventapis.api.EventRepository;
import com.kloia.eventapis.api.ViewQuery;
import com.kloia.eventapis.cassandra.ConcurrentEventException;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.exception.EventPulisherException;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.view.EntityFunctionSpec;
import com.kloia.sample.dto.event.PaymentProcessEvent;
import com.kloia.sample.dto.event.StockReservedEvent;
import com.kloia.sample.model.Order;
import com.kloia.sample.model.OrderState;
import com.kloia.sample.model.PaymentInformation;
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
public class StockReservedEventHandler implements EventHandler< StockReservedEvent> {
    private final static String name = "PROCESS_ORDER";
    private final static String CREATED = "CREATED";
    private final EventRepository eventRepository;
    private final ViewQuery<Order> orderQuery;

    @Autowired
    public StockReservedEventHandler(EventRepository eventRepository, ViewQuery<Order> orderQuery) {
        this.eventRepository = eventRepository;
        this.orderQuery = orderQuery;
    }

    @Override
    @KafkaListener(topics = "StockReservedEvent", containerFactory = "eventsKafkaListenerContainerFactory")
    public EventKey execute(StockReservedEvent dto) throws EventStoreException, EventPulisherException, ConcurrentEventException {
        Order order = orderQuery.queryEntity(dto.getOrderId());

        if (order.getState() == OrderState.PROCESSING) {
            PaymentProcessEvent paymentProcessEvent = new PaymentProcessEvent(order.getId(),new PaymentInformation(order.getPaymentAddress(),order.getAmount(),order.getCardInformation()));
            log.info("Payment is processing : " + dto);
            return eventRepository.recordAndPublish(order,paymentProcessEvent);
        } else
            throw new EventStoreException("Order state is not valid for this Operation: " + dto);
    }

    @Component
    public static class PaymentProcessSpec extends EntityFunctionSpec<Order, PaymentProcessEvent> {
        public PaymentProcessSpec() {
            super((order, event) -> {
                PaymentProcessEvent eventData = event.getEventData();
                order.setState(OrderState.PAYMENT_READY);
                return order;
            });
        }
    }
}
