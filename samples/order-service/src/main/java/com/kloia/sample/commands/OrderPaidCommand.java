package com.kloia.sample.commands;

import com.kloia.eventapis.pojos.EventKey;
import com.kloia.evented.Command;
import com.kloia.evented.EntityFunctionSpec;
import com.kloia.evented.EventPulisherException;
import com.kloia.evented.EventRepository;
import com.kloia.evented.EventStoreException;
import com.kloia.evented.Query;
import com.kloia.sample.dto.command.OrderPaidCommandDto;
import com.kloia.sample.dto.event.OrderPaidEvent;
import com.kloia.sample.dto.event.PaymentSuccessEvent;
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
public class OrderPaidCommand implements Command<Order, PaymentSuccessEvent> {
    private final static String name = "PROCESS_ORDER";
    private final static String CREATED = "CREATED";
    private final EventRepository<Order> eventRepository;
    private final Query<Order> orderQuery;

    @Autowired
    public OrderPaidCommand(EventRepository<Order> eventRepository, Query<Order> orderQuery) {
        this.eventRepository = eventRepository;
        this.orderQuery = orderQuery;
    }

    @Override
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
