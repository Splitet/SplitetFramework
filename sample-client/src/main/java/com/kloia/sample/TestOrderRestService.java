package com.kloia.sample;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.api.impl.OperationRepository;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.evented.*;
import com.kloia.sample.commands.CreateOrderCommand;
import com.kloia.sample.commands.ProcessOrderCommand;
import com.kloia.sample.dto.Order;
import com.kloia.sample.dto.OrderCreateAggDTO;
import com.kloia.sample.dto.OrderProcessAggDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.springframework.cloud.netflix.feign.EnableFeignClients;


/**
 * Created by zeldalozdemir on 09/02/2017.
 */
@Slf4j
@RestController
@RequestMapping(value = "/aggr/v1/order/")
@EnableFeignClients
public class TestOrderRestService {

    @Autowired
    private IEventRepository<com.kloia.sample.dto.Order> eventRepository;

    @Autowired
    OperationRepository operationRepository;

    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    PaymentEndpoint paymentEndpoint;

    @Autowired
    CreateOrderCommand createOrderCommand;

    @Autowired
    ProcessOrderCommand processOrderCommand;


    @PostConstruct
    public void initTestOrderRestService() {
        eventRepository.addAggregateSpecs(createOrderCommand);
        eventRepository.addAggregateSpecs(processOrderCommand);
    }

    @RequestMapping(value = "/{orderId}", method = RequestMethod.GET)
    public ResponseEntity<?> getOrder(@PathVariable("orderId") Long orderId, @RequestBody @Valid OrderCreateAggDTO orderCreateAggDTO) throws IOException, EventStoreException {

        return new ResponseEntity<Object>(eventRepository.queryEntity(orderId), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity<?> aggregateCreateOrder(@RequestBody @Valid OrderCreateAggDTO orderCreateAggDTO) throws IOException, EventStoreException {
//        TemplateAccount saved = createTemplateAccountService.create(orderCreateDTO);
        log.info("Template account saved: " + orderCreateAggDTO);


        createOrderCommand.processCommand(orderCreateAggDTO);

        return new ResponseEntity<Object>(eventRepository.queryEntity(orderCreateAggDTO.getOrderId()), HttpStatus.CREATED);
    }


    @RequestMapping(value = "/process", method = RequestMethod.POST)
    public ResponseEntity<?> aggregateProcessOrder(@RequestBody @Valid OrderProcessAggDTO orderProcessAggDTO) throws Exception {
//        TemplateAccount saved = createTemplateAccountService.create(orderCreateDTO);
        processOrderCommand.processCommand(orderProcessAggDTO);
        paymentEndpoint.process(orderProcessAggDTO);

        return new ResponseEntity<Object>(eventRepository.queryEntity(orderProcessAggDTO.getOrderId()), HttpStatus.CREATED);


    }


}

