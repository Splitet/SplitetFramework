package com.kloia.sample.controller;

import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.cassandra.PersistentEventRepository;
import com.kloia.sample.commands.DoPaymentCommand;
import com.kloia.sample.model.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
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
@RequestMapping(value = "/payment/v1/")
@EnableFeignClients
public class PaymentRestController {

    @Autowired
    private PersistentEventRepository<Payment> orderEventRepository;

    @Autowired
    private DoPaymentCommand doPaymentCommand;

    @RequestMapping(value = "/{paymentId}", method = RequestMethod.GET)
    public ResponseEntity<?> getPayment(@PathVariable("paymentId") String paymentId) throws IOException, EventStoreException {
        return new ResponseEntity<Object>(orderEventRepository.queryEntity(paymentId), HttpStatus.CREATED);
    }




}

