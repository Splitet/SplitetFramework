package com.kloia.sample.controller;

import com.kloia.eventapis.api.ViewQuery;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.sample.model.Orders;
import com.kloia.sample.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


/**
 * Created by zeldalozdemir on 09/02/2017.
 */
@Slf4j
@RestController
@RequestMapping(value = "/order/")
public class OrderRestController {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ViewQuery<Orders> orderViewQuery;

    @RequestMapping(value = "/{orderId}", method = RequestMethod.GET)
    public ResponseEntity<?> getOrder(@PathVariable("orderId") String orderId) throws IOException, EventStoreException {
        return new ResponseEntity<Object>(orderRepository.findOne(orderId), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> getOrders() throws IOException, EventStoreException {
        return new ResponseEntity<Object>(orderRepository.findAll(), HttpStatus.OK);
    }

    @RequestMapping(value = "/{orderId}/{version}", method = RequestMethod.GET)
    public ResponseEntity<?> getOrderWithVersion(@PathVariable("orderId") String orderId, @PathVariable("version") Integer version) throws IOException, EventStoreException {
        return new ResponseEntity<Object>(orderViewQuery.queryEntity(orderId, version), HttpStatus.OK);
    }

}

