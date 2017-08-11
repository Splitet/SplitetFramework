package com.kloia.sample.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.cassandra.PersistentEventRepository;
import com.kloia.sample.client.PaymentEndpoint;
import com.kloia.sample.client.StockEndpoint;
import com.kloia.sample.commands.CreateOrderCommand;
import com.kloia.sample.commands.ProcessOrderCommand;
import com.kloia.sample.dto.command.CreateOrderCommandDto;
import com.kloia.sample.dto.command.ProcessOrderCommandDto;
import com.kloia.sample.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;


/**
 * Created by zeldalozdemir on 09/02/2017.
 */
@Slf4j
@RestController
@RequestMapping(value = "/order/v1/")
@EnableFeignClients
public class OrderRestController {

    @Autowired
    private PersistentEventRepository<Order> orderEventRepository;


    @RequestMapping(value = "/{orderId}", method = RequestMethod.GET)
    public ResponseEntity<?> getOrder(@PathVariable("orderId") String orderId) throws IOException, EventStoreException {
        return new ResponseEntity<Object>(orderEventRepository.queryEntity(orderId), HttpStatus.CREATED);
    }

}

