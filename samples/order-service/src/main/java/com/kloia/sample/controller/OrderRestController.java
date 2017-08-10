package com.kloia.sample.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.evented.EventStoreException;
import com.kloia.evented.IEventRepository;
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
    private IEventRepository<Order> orderEventRepository;


    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    PaymentEndpoint paymentEndpoint;

    @Autowired
    StockEndpoint stockEndpoint;

    @Autowired
    CreateOrderCommand createOrderCommand;

    @Autowired
    ProcessOrderCommand processOrderCommand;


    @RequestMapping(value = "/{orderId}", method = RequestMethod.GET)
    public ResponseEntity<?> getOrder(@PathVariable("orderId") String orderId) throws IOException, EventStoreException {

        return new ResponseEntity<Object>(orderEventRepository.queryEntity(orderId), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity<?> createOrder(@RequestBody @Valid CreateOrderCommandDto orderCreateAggDTO) throws Exception {
//        TemplateAccount saved = createTemplateAccountService.create(orderCreateDTO);
        log.info("Template account saved: " + orderCreateAggDTO);

        String id = createOrderCommand.execute(orderCreateAggDTO).getEntityId();

        return new ResponseEntity<Object>(orderEventRepository.queryEntity(id), HttpStatus.CREATED);
    }


    @RequestMapping(value = "/{orderId}/process", method = RequestMethod.POST)
    public ResponseEntity<?> processOrder(@PathVariable("orderId") String orderId, @RequestBody @Valid ProcessOrderCommandDto processOrderCommandDto) throws Exception {
        processOrderCommandDto.setOrderId(orderId);
        processOrderCommand.execute(processOrderCommandDto);

        return new ResponseEntity<Object>(orderEventRepository.queryEntity(processOrderCommandDto.getOrderId()), HttpStatus.CREATED);


    }


}

