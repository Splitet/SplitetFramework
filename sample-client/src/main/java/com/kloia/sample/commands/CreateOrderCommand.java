package com.kloia.sample.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.api.impl.OperationRepository;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.evented.*;
import com.kloia.sample.dto.Order;
import com.kloia.sample.dto.OrderCreateAggDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
@Slf4j
@Controller
public class CreateOrderCommand extends CommandSpec<Order, OrderCreateAggDTO> {
    private final static String name = "CREATE_ORDER";
    private OperationRepository operationRepository;

    @Autowired
    public CreateOrderCommand(ObjectMapper objectMapper, OperationRepository operationRepository, IEventRepository<Order> eventRepository) {
        super(name,objectMapper,eventRepository, (order, event) -> {
            try {
                OrderCreateAggDTO orderCreateAggDTO = objectMapper.readerFor(OrderCreateAggDTO.class).readValue(event.getEventData());
                order = new Order();
                order.setOrderId(orderCreateAggDTO.getOrderId());
                order.setOrderAmount(orderCreateAggDTO.getOrderAmount());
                order.setDescription(orderCreateAggDTO.getDescription());
                order.setState("CREATED");
                return order;
            } catch (Exception e) {
                log.error("Error while applying Aggregate:" + event + " Exception:" + e.getMessage(), e);
                throw new EventStoreException("Error while applying Aggregate:" + event + " Exception:" + e.getMessage(), e);
            }
        });

        this.operationRepository = operationRepository;
    }

    @Override
    public void processCommand(OrderCreateAggDTO orderCreateAggDTO) throws EventStoreException {
        try {
            Map.Entry<UUID, Operation> context = operationRepository.getContext();
            EntityEvent entityEvent = createEvent(new EventKey(orderCreateAggDTO.getOrderId(),0),"CREATED", orderCreateAggDTO,context.getKey());
            getEventRepository().recordAggregateEvent(entityEvent);
        } catch (Exception e) {
            throw new EventStoreException("Error while processing Command:" + orderCreateAggDTO);
        }
    }


}
