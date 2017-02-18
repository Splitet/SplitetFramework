package com.kloia.sample;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.api.StoreApi;
import com.kloia.eventapis.api.impl.OperationRepository;
import com.kloia.eventapis.api.pojos.Operation;
import com.kloia.evented.AggregateEvent;
import com.kloia.evented.AggregateKey;
import com.kloia.evented.AggregateRepository;
import com.kloia.sample.dto.Order;
import com.kloia.sample.dto.OrderCreateAggDTO;
import com.kloia.sample.dto.OrderProcessAggDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 09/02/2017.
 */
@Slf4j
@RestController()
@RequestMapping(value = "/aggr/v1/order/")
public class TestOrderRestService {

    @Autowired
    private AggregateRepository<Order> aggregateRepository;

    @Autowired
    private StoreApi storeApi;

    @Autowired
    private ObjectMapper objectMapper;

    public TestOrderRestService() {


    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity<?> aggregateCreateOrder(@RequestBody @Valid OrderCreateAggDTO orderCreateAggDTO) throws JsonProcessingException {
//        TemplateAccount saved = createTemplateAccountService.create(orderCreateDTO);
        log.info("Template account saved: " + orderCreateAggDTO);
        OperationRepository operationRepository = storeApi.getOperationRepository();

        Map.Entry<UUID, Operation> context = operationRepository.getContext();
        String description = objectMapper.writer().writeValueAsString(orderCreateAggDTO);
        AggregateEvent aggregateEvent = new AggregateEvent(new AggregateKey(orderCreateAggDTO.getOrderId(),new Date(), context.getKey(),"CREATE_ORDER"), "CREATED", description);
        AggregateEvent eventRecorded = aggregateRepository.recordAggregate(aggregateEvent);
        return new ResponseEntity<Object>(eventRecorded, HttpStatus.CREATED);
    }


    @RequestMapping(value = "/process", method = RequestMethod.POST)
    public ResponseEntity<?> aggregateProcessOrder(@RequestBody @Valid OrderProcessAggDTO orderProcessAggDTO) throws IOException {
//        TemplateAccount saved = createTemplateAccountService.create(orderCreateDTO);
        Order order = aggregateRepository.getAggregate(orderProcessAggDTO.getOrderId(),Order.class);
        log.info("Template account saved: " + orderProcessAggDTO);
        return new ResponseEntity<Object>(order, HttpStatus.OK);
    }

}
