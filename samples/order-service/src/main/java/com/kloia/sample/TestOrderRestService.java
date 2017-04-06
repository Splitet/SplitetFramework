package com.kloia.sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.api.impl.OperationRepository;
import com.kloia.evented.EventStoreException;
import com.kloia.evented.IEventRepository;
import com.kloia.sample.commands.CreateOrderCommand;
import com.kloia.sample.commands.ProcessOrderCommand;
import com.kloia.sample.dto.Order;
import com.kloia.sample.dto.OrderCreateAggDTO;
import com.kloia.sample.dto.OrderProcessAggDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.io.IOException;


/**
 * Created by zeldalozdemir on 09/02/2017.
 */
@Slf4j
@RestController
@RequestMapping(value = "/aggr/v1/order/")
@EnableFeignClients
public class TestOrderRestService {

    @Autowired
    @Qualifier("orderEventRepository")
    private IEventRepository<Order> orderEventRepository;

    @Autowired
    OperationRepository operationRepository;

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


    @PostConstruct
    public void initTestOrderRestService() {
        orderEventRepository.addAggregateSpecs(createOrderCommand);
        orderEventRepository.addAggregateSpecs(processOrderCommand);
    }

    @RequestMapping(value = "/{orderId}", method = RequestMethod.GET)
    public ResponseEntity<?> getOrder(@PathVariable("orderId") Long orderId) throws IOException, EventStoreException {

        return new ResponseEntity<Object>(orderEventRepository.queryEntity(orderId), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity<?> aggregateCreateOrder(@RequestBody @Valid OrderCreateAggDTO orderCreateAggDTO) throws IOException, EventStoreException {
//        TemplateAccount saved = createTemplateAccountService.create(orderCreateDTO);
        log.info("Template account saved: " + orderCreateAggDTO);

        createOrderCommand.processCommand(orderCreateAggDTO);

        return new ResponseEntity<Object>(orderEventRepository.queryEntity(orderCreateAggDTO.getOrderId()), HttpStatus.CREATED);
    }


    @RequestMapping(value = "/process", method = RequestMethod.POST)
    public ResponseEntity<?> aggregateProcessOrder(@RequestBody @Valid OrderProcessAggDTO orderProcessAggDTO) throws Exception {

        processOrderCommand.processCommand(orderProcessAggDTO);
        stockEndpoint.process(orderProcessAggDTO.getItemSoldAggDTO());
        paymentEndpoint.process(orderProcessAggDTO.getPaymentProcessAggDTO());

        return new ResponseEntity<Object>(orderEventRepository.queryEntity(orderProcessAggDTO.getOrderId()), HttpStatus.CREATED);


    }


}

