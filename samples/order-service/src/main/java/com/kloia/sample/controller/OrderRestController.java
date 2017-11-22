package com.kloia.sample.controller;

import com.kloia.eventapis.api.ViewQuery;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.sample.model.Order;
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
@RequestMapping(value = "/order/v1/")
public class OrderRestController {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ViewQuery<Order> orderViewQuery;

    @RequestMapping(value = "/{orderId}", method = RequestMethod.GET)
    public ResponseEntity<?> getOrder(@PathVariable("orderId") String orderId) throws IOException, EventStoreException {
        return new ResponseEntity<Object>(orderRepository.findOne(orderId), HttpStatus.OK);
    }

    @RequestMapping(value = "/{orderId}/{version}", method = RequestMethod.GET)
    public ResponseEntity<?> getOrderWithVersion(@PathVariable("orderId") String orderId, @PathVariable("version") Integer version) throws IOException, EventStoreException {
        return new ResponseEntity<Object>(orderViewQuery.queryEntity(orderId, version), HttpStatus.OK);
    }

}

