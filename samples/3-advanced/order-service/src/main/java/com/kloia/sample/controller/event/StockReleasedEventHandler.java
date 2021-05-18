package com.kloia.sample.controller.event;

import com.kloia.eventapis.api.EventHandler;
import com.kloia.eventapis.api.EventRepository;
import com.kloia.eventapis.api.ViewQuery;
import com.kloia.eventapis.cassandra.ConcurrentEventException;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.view.EntityFunctionSpec;
import com.kloia.sample.client.PaymentClient;
import com.kloia.sample.dto.command.ReturnPaymentCommandDto;
import com.kloia.sample.dto.event.OrderCancelledEvent;
import com.kloia.sample.dto.event.StockReleasedEvent;
import com.kloia.sample.model.OrderState;
import com.kloia.sample.model.Orders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
@Slf4j
@RestController
public class StockReleasedEventHandler implements EventHandler<StockReleasedEvent> {

    private final EventRepository eventRepository;
    private final ViewQuery<Orders> orderQuery;
    private final PaymentClient paymentClient;

    public StockReleasedEventHandler(EventRepository eventRepository, ViewQuery<Orders> orderQuery, PaymentClient paymentClient) {
        this.eventRepository = eventRepository;
        this.orderQuery = orderQuery;
        this.paymentClient = paymentClient;
    }


    @KafkaListener(topics = "StockReleasedEvent", containerFactory = "eventsKafkaListenerContainerFactory")
    public EventKey execute(StockReleasedEvent dto) throws EventStoreException, ConcurrentEventException {
        Orders order = orderQuery.queryEntity(dto.getOrderId());

        if (order.getState() == OrderState.RELEASING_STOCK) {
            OrderCancelledEvent orderCancelledEvent = new OrderCancelledEvent();
            log.info("Payment is processing : " + dto);
            EventKey eventKey = eventRepository.recordAndPublish(order, orderCancelledEvent);
            paymentClient.returnPaymentCommand(order.getPaymentId(), new ReturnPaymentCommandDto(order.getId()));
            return eventKey;
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
