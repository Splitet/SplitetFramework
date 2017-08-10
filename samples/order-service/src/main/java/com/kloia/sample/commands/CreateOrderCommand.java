package com.kloia.sample.commands;

import com.kloia.eventapis.api.Command;
import com.kloia.eventapis.view.EntityFunctionSpec;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.api.EventRepository;
import com.kloia.eventapis.api.Query;
import com.kloia.sample.dto.command.CreateOrderCommandDto;
import com.kloia.sample.dto.event.OrderCreatedEvent;
import com.kloia.sample.model.Order;
import com.kloia.sample.model.OrderState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
@Slf4j
@Controller
public class CreateOrderCommand implements Command<Order, CreateOrderCommandDto> {
    private final static String name = "CREATE_ORDER";
    private final static String CREATED = "CREATED";
    private final EventRepository<Order> eventRepository;
    private final Query<Order> orderQuery;


    @Autowired
    public CreateOrderCommand(EventRepository<Order> eventRepository, Query<Order> orderQuery) {
        this.eventRepository = eventRepository;
        this.orderQuery = orderQuery;
    }

    @Override
    public EventKey execute(CreateOrderCommandDto dto) throws Exception {

        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent();
        BeanUtils.copyProperties(dto,orderCreatedEvent);

        EventKey eventKey = eventRepository.recordAndPublish(orderCreatedEvent);


        return eventKey;
    }
    @Component
    public static class CreateOrderSpec extends EntityFunctionSpec<Order, OrderCreatedEvent> {
        public CreateOrderSpec() {
            super((order, event) -> {
                OrderCreatedEvent createOrderCommandDto = event.getEventData();
                order.setStockId(createOrderCommandDto.getStockId());
                order.setOrderAmount(createOrderCommandDto.getOrderAmount());
                order.setDescription(createOrderCommandDto.getDescription());
                order.setState(OrderState.INITIAL);
                return order;
            });
        }
    }
}
