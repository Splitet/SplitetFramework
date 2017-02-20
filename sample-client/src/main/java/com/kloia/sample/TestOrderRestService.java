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
import feign.Feign;
import feign.RequestLine;
import feign.codec.Decoder;
import feign.codec.Encoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.feign.support.ResponseEntityDecoder;
import org.springframework.cloud.netflix.feign.support.SpringDecoder;
import org.springframework.cloud.netflix.feign.support.SpringEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Created by zeldalozdemir on 09/02/2017.
 */
@Slf4j
@SpringBootApplication
@RequestMapping(value = "/aggr/v1/order/")
@SpringBootConfiguration()
@EnableFeignClients
public class TestOrderRestService {

    @Autowired
    private AggregateRepository<Order> aggregateRepository;

    @Autowired
    private StoreApi storeApi;

    @Autowired
    private ObjectMapper objectMapper;


    @Autowired PaymentEndpoint paymentEndpoint;

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
//        AggregateEvent eventRecorded = aggregateRepository.recordAggregate(aggregateEvent);
//        PaymentEndpoint paymentEndpoint = Feign.builder().decoder(feignDecoder()).encoder(feignEncoder()).target(PaymentEndpoint.class, "http://localhost:8080");
        OrderCreateAggDTO orderCreateAggDTO1 = paymentEndpoint.process(orderCreateAggDTO);
        return new ResponseEntity<Object>(orderCreateAggDTO1, HttpStatus.CREATED);
    }


    @RequestMapping(value = "/process", method = RequestMethod.POST)
    public ResponseEntity<?> aggregateProcessOrder(@RequestBody @Valid OrderProcessAggDTO orderProcessAggDTO) throws IOException {
//        TemplateAccount saved = createTemplateAccountService.create(orderCreateDTO);
        Order order = aggregateRepository.getAggregate(orderProcessAggDTO.getOrderId(),Order.class);
        log.info("Template account saved: " + orderProcessAggDTO);
        return new ResponseEntity<Object>(order, HttpStatus.OK);
    }


}

