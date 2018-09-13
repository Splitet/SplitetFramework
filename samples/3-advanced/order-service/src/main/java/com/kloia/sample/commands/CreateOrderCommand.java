package com.kloia.sample.commands;

import com.kloia.eventapis.api.Command;
import com.kloia.eventapis.api.CommandHandler;
import com.kloia.eventapis.api.EventRepository;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.spring.configuration.DataMigrationService;
import com.kloia.eventapis.view.Entity;
import com.kloia.eventapis.view.EntityFunctionSpec;
import com.kloia.sample.dto.command.CreateOrderCommandDto;
import com.kloia.sample.dto.event.OrderCreatedEvent;
import com.kloia.sample.model.OrderState;
import com.kloia.sample.model.Orders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.ws.rs.QueryParam;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
@Slf4j
@RestController
public class CreateOrderCommand implements CommandHandler {
    private final EventRepository eventRepository;
    private final DataMigrationService dataMigrationService;

    @Autowired
    public CreateOrderCommand(EventRepository eventRepository, DataMigrationService dataMigrationService) {
        this.eventRepository = eventRepository;
        this.dataMigrationService = dataMigrationService;
    }

    @RequestMapping(value = "/order/create", method = RequestMethod.POST)
    @Command
    public EventKey execute(@RequestBody @Valid CreateOrderCommandDto dto) throws Exception {

        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent();
        BeanUtils.copyProperties(dto, orderCreatedEvent);

        return eventRepository.recordAndPublish(orderCreatedEvent);
    }

    @RequestMapping(value = "/order/{entityId}/{version}/migrate", method = RequestMethod.POST)
    public Entity migrate(@PathVariable("entityId") String entityId,
                          @PathVariable("version") Integer version,
                          @QueryParam("snapshot") Boolean snapshot,
                          @RequestBody CreateOrderCommandDto dto) throws Exception {

        return dataMigrationService.updateEvent(new EventKey(entityId, version), true, new OrderCreatedEvent(
                dto.getStockId(), dto.getOrderAmount(), dto.getDescription()
        ));
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
