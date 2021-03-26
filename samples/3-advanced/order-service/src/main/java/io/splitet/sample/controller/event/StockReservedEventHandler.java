package io.splitet.sample.controller.event;

import io.splitet.core.api.EventHandler;
import io.splitet.core.api.EventRepository;
import io.splitet.core.api.ViewQuery;
import io.splitet.core.cassandra.ConcurrentEventException;
import io.splitet.core.common.EventKey;
import io.splitet.core.exception.EventStoreException;
import io.splitet.core.view.EntityFunctionSpec;
import io.splitet.sample.dto.event.PaymentProcessEvent;
import io.splitet.sample.dto.event.StockReservedEvent;
import io.splitet.sample.model.OrderState;
import io.splitet.sample.model.Orders;
import io.splitet.sample.model.PaymentInformation;
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
public class StockReservedEventHandler implements EventHandler<StockReservedEvent> {

    private final EventRepository eventRepository;
    private final ViewQuery<Orders> orderQuery;

    @Autowired
    public StockReservedEventHandler(EventRepository eventRepository, ViewQuery<Orders> orderQuery) {
        this.eventRepository = eventRepository;
        this.orderQuery = orderQuery;
    }

    @Override
    @KafkaListener(topics = "StockReservedEvent", containerFactory = "eventsKafkaListenerContainerFactory")
    public EventKey execute(StockReservedEvent dto) throws EventStoreException, ConcurrentEventException {
        Orders order = orderQuery.queryEntity(dto.getOrderId());

        if (order.getState() == OrderState.RESERVING_STOCK) {
            PaymentProcessEvent paymentProcessEvent = new PaymentProcessEvent(
                    order.getId(),
                    dto.getSender().getVersion(),
                    new PaymentInformation(order.getPaymentAddress(), order.getAmount(), order.getCardInformation()));
            log.info("Payment is processing : " + dto);
            return eventRepository.recordAndPublish(order, paymentProcessEvent);
        } else
            throw new EventStoreException("Order state is not valid for this Operation: " + dto);
    }

    @Component
    public static class PaymentProcessSpec extends EntityFunctionSpec<Orders, PaymentProcessEvent> {
        public PaymentProcessSpec() {
            super((order, event) -> {
                PaymentProcessEvent eventData = event.getEventData();
                order.setReservedStockVersion(eventData.getReservedStockVersion());
                order.setState(OrderState.PAYMENT_READY);
                return order;
            });
        }
    }
}
