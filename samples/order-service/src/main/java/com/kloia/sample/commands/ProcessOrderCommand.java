package com.kloia.sample.commands;

import com.kloia.eventapis.pojos.EventKey;
import com.kloia.evented.Command;
import com.kloia.evented.EntityFunctionSpec;
import com.kloia.evented.EventRepository;
import com.kloia.evented.EventStoreException;
import com.kloia.evented.Query;
import com.kloia.sample.dto.command.ProcessOrderCommandDto;
import com.kloia.sample.dto.event.ReserveStockEvent;
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
public class ProcessOrderCommand implements Command<Order, ProcessOrderCommandDto> {
    private final static String name = "PROCESS_ORDER";
    private final static String CREATED = "CREATED";
    private final EventRepository<Order> eventRepository;
    private final Query<Order> orderQuery;

    @Autowired
    public ProcessOrderCommand(EventRepository<Order> eventRepository, Query<Order> orderQuery) {
        this.eventRepository = eventRepository;
        this.orderQuery = orderQuery;
    }

    @Override
    public EventKey execute(ProcessOrderCommandDto dto) throws Exception {
        Order order = orderQuery.queryEntity(dto.getOrderId());

        if (order.getState() == OrderState.INITIAL) {
            ReserveStockEvent reserveStockEvent = new ReserveStockEvent(order.getStockId(), order.getOrderAmount(), dto.getPaymentInformation());
            log.info("Template account saved: " + dto);
            return eventRepository.recordAndPublish(order,reserveStockEvent);
        } else
            throw new EventStoreException("Order state is not valid for this Operation: " + dto);
    }

    @Component
    public static class ProcessOrderSpec extends EntityFunctionSpec<Order, ReserveStockEvent> {
        public ProcessOrderSpec() {
            super((order, event) -> {
                ReserveStockEvent eventData = event.getEventData();
                order.setPaymentInformation(eventData.getPaymentInformation());
                order.setState(OrderState.PROCESSING);
                return order;
            });
        }
    }
}
