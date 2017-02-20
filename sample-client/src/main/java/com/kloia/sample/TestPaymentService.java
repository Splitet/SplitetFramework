package com.kloia.sample;

import com.kloia.eventapis.api.StoreApi;
import com.kloia.eventapis.api.impl.OperationRepository;
import com.kloia.eventapis.api.pojos.Operation;
import com.kloia.evented.AggregateEvent;
import com.kloia.evented.AggregateKey;
import com.kloia.evented.AggregateRepository;
import com.kloia.sample.dto.OrderCreateAggDTO;
import com.kloia.sample.dto.OrderProcessAggDTO;
import com.kloia.sample.dto.PaymentProcessAggDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 09/02/2017.
 */
@Slf4j
@RequestMapping(value = "/aggr/v1/payment/")
@SpringBootApplication
public class TestPaymentService {

    @Autowired
    private AggregateRepository aggregateRepository;

    @Autowired
    private StoreApi storeApi;

    public TestPaymentService() {


    }


    @RequestMapping(value = "/process", method = RequestMethod.POST)
    public ResponseEntity<?> aggregateProcessOrder(@RequestBody @Valid PaymentProcessAggDTO paymentProcessAggDTO) {
//        TemplateAccount saved = createTemplateAccountService.create(orderCreateDTO);
        log.info("PaymentProcessAggDTO saved: " + paymentProcessAggDTO);
        return new ResponseEntity<Object>(paymentProcessAggDTO, HttpStatus.OK);
    }
}
