package io.splitet.sample.commands;

import io.splitet.core.api.Command;
import io.splitet.core.api.CommandHandler;
import io.splitet.core.api.EventRepository;
import io.splitet.core.common.EventKey;
import io.splitet.core.view.EntityFunctionSpec;
import io.splitet.sample.dto.command.CreateOrderCommandDto;
import io.splitet.sample.dto.event.OrderCreatedEvent;
import io.splitet.sample.model.OrderState;
import io.splitet.sample.model.Orders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
@Slf4j
@RestController
public class CreateOrderCommand implements CommandHandler {

    private final EventRepository eventRepository;

    @Autowired
    public CreateOrderCommand(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @RequestMapping(value = "/order/create", method = RequestMethod.POST)
    @Command
    public EventKey execute(@RequestBody @Valid CreateOrderCommandDto dto) throws Exception {
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent();
        BeanUtils.copyProperties(dto, orderCreatedEvent);

        return eventRepository.recordAndPublish(orderCreatedEvent);
    }

    @Component
    public static class CreateOrderSpec extends EntityFunctionSpec<Orders, OrderCreatedEvent> {
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
