package com.kloia.sample.controller;

import com.kloia.eventapis.api.ViewQuery;
import com.kloia.eventapis.cassandra.EntityEvent;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.spring.configuration.DataMigrationService;
import com.kloia.eventapis.view.Entity;
import com.kloia.sample.dto.command.CreateOrderCommandDto;
import com.kloia.sample.dto.event.OrderCreatedEvent;
import com.kloia.sample.model.Orders;
import com.kloia.sample.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.util.List;


/**
 * Created by zeldalozdemir on 09/02/2017.
 */
@Slf4j
@RestController
@RequestMapping(value = "/order/")
public class OrderRestController {

    private final OrderRepository orderRepository;
    private final ViewQuery<Orders> orderViewQuery;
    private final DataMigrationService dataMigrationService;

    public OrderRestController(OrderRepository orderRepository,
                               ViewQuery<Orders> orderViewQuery,
                               DataMigrationService dataMigrationService) {
        this.orderRepository = orderRepository;
        this.orderViewQuery = orderViewQuery;
        this.dataMigrationService = dataMigrationService;
    }

    @RequestMapping(value = "/{orderId}", method = RequestMethod.GET)
    public ResponseEntity<?> getOrder(@PathVariable("orderId") String orderId) {
        Orders orders = orderRepository.findOne(orderId);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> getOrders() {
        List<Orders> orders = orderRepository.findAll();
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @RequestMapping(value = "/{orderId}/{version}", method = RequestMethod.GET)
    public ResponseEntity<?> getOrderWithVersion(@PathVariable("orderId") String orderId, @PathVariable("version") Integer version) throws IOException, EventStoreException {
        Orders orders = orderViewQuery.queryEntity(orderId, version);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @RequestMapping(value = "/{orderId}/history", method = RequestMethod.GET)
    public ResponseEntity<?> getOrderHistory(@PathVariable("orderId") String orderId) throws Exception {
        List<EntityEvent> historyEvents = orderViewQuery.queryHistory(orderId);
        return new ResponseEntity<>(historyEvents, HttpStatus.OK);
    }

    @RequestMapping(value = "/order/{entityId}/{version}/migrate", method = RequestMethod.POST)
    public Entity migrate(
            @PathVariable("entityId") String entityId,
            @PathVariable("version") Integer version,
            @QueryParam("snapshot") Boolean snapshot,
            @RequestBody CreateOrderCommandDto dto
    ) throws Exception {
        return dataMigrationService.updateEvent(new EventKey(entityId, version), true, new OrderCreatedEvent(
                dto.getStockId(), dto.getOrderAmount(), dto.getDescription()
        ));
    }

}

