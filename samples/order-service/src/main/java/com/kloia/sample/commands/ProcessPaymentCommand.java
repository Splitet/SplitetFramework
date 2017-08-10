package com.kloia.sample.commands;

import com.kloia.eventapis.pojos.EventKey;
import com.kloia.evented.Command;
import com.kloia.evented.EntityFunctionSpec;
import com.kloia.evented.EventPulisherException;
import com.kloia.evented.EventRepository;
import com.kloia.evented.EventStoreException;
import com.kloia.evented.Query;
import com.kloia.sample.dto.event.PaymentProcessEvent;
import com.kloia.sample.dto.command.ProcessOrderPaymentCommandDto;
import com.kloia.sample.dto.event.StockReservedEvent;
import com.kloia.sample.model.Order;
import com.kloia.sample.model.OrderState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
@Slf4j
@Controller
public class ProcessPaymentCommand implements Command<Order, StockReservedEvent> {
    private final static String name = "PROCESS_ORDER";
    private final static String CREATED = "CREATED";
    private final EventRepository<Order> eventRepository;
    private final Query<Order> orderQuery;

    @Autowired
    public ProcessPaymentCommand(EventRepository<Order> eventRepository, Query<Order> orderQuery) {
        this.eventRepository = eventRepository;
        this.orderQuery = orderQuery;
    }

    @Override
    public EventKey execute(StockReservedEvent dto) throws EventStoreException, EventPulisherException {
        Order order = orderQuery.queryEntity(dto.getOrderId());

        if (order.getState() == OrderState.PROCESSING) {
            PaymentProcessEvent paymentProcessEvent = new PaymentProcessEvent(order.getId(),order.getPaymentInformation());
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
