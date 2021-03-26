package io.splitet.sample.controller.event;

import io.splitet.core.api.EventHandler;
import io.splitet.core.api.EventRepository;
import io.splitet.core.api.ViewQuery;
import io.splitet.core.cassandra.ConcurrentEventException;
import io.splitet.core.common.EventKey;
import io.splitet.core.exception.EventStoreException;
import io.splitet.core.view.EntityFunctionSpec;
import io.splitet.sample.client.PaymentClient;
import io.splitet.sample.dto.command.ReturnPaymentCommandDto;
import io.splitet.sample.dto.event.OrderCancelledEvent;
import io.splitet.sample.dto.event.StockReleasedEvent;
import io.splitet.sample.model.OrderState;
import io.splitet.sample.model.Orders;
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
