package com.kloia.sample.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.api.impl.OperationRepository;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.evented.*;
import com.kloia.sample.dto.Order;
import com.kloia.sample.dto.OrderProcessAggDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
@Slf4j
@Controller
public class ProcessOrderCommand extends CommandSpec<Order, OrderProcessAggDTO> {
    private final static String name = "PROCESS_ORDER";
    private OperationRepository operationRepository;

    public ProcessOrderCommand(ObjectMapper objectMapper, OperationRepository operationRepository, IEventRepository<Order> eventRepository) {
        super(name, objectMapper, eventRepository, (order, event) -> {
            try {
                OrderProcessAggDTO orderProcessAggDTO = objectMapper.readerFor(OrderProcessAggDTO.class).readValue(event.getEventData());
                order.setAddress(orderProcessAggDTO.getAddress());
                order.setPrice(orderProcessAggDTO.getPrice());
                order.setState("PROCESSED");
                return order;
            } catch (Exception e) {
                log.error("Error while applying Aggregate:" + event + " Exception:" + e.getMessage(), e);
                throw new EventStoreException("Error while applying Aggregate:" + event + " Exception:" + e.getMessage(), e);
            }
        });
        this.operationRepository = operationRepository;
    }

    @Override
    public void processCommand(OrderProcessAggDTO eventDto) throws EventStoreException {

        Order order = eventRepository.queryEntity(eventDto.getOrderId());

        if (order.getState().equals("CREATED")) {
            try {
                Map.Entry<UUID, Operation> context = operationRepository.getContext();
                EntityEvent entityEvent = createEvent(new EventKey(eventDto.getOrderId(), order.getVersion() + 1), "CREATED", eventDto, context.getKey());
                getEventRepository().recordAggregateEvent(entityEvent);
                log.info("Template account saved: " + eventDto);
            } catch (JsonProcessingException e) {
                throw new EventStoreException("Error while processing Command:" + e.getMessage(), e);
            }
        } else
            throw new EventStoreException("Order state is not valid for this Operation: " + eventDto);

    }
}
