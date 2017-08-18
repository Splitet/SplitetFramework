package com.kloia.sample.controller;

import com.kloia.eventapis.api.ViewQuery;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.sample.model.Payment;
import com.kloia.sample.repository.PaymentRepository;
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
    private ViewQuery<Payment> orderEventRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @RequestMapping(value = "/{paymentId}", method = RequestMethod.GET)
    public ResponseEntity<?> getPayment(@PathVariable("paymentId") String paymentId) throws IOException, EventStoreException {
        return new ResponseEntity<Object>(paymentRepository.findOne(paymentId), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{paymentId}/{version}", method = RequestMethod.GET)
    public ResponseEntity<?> getPaymentWithVersion(@PathVariable("paymentId") String paymentId, @PathVariable("version") Integer version) throws IOException, EventStoreException {
        return new ResponseEntity<Object>(orderEventRepository.queryEntity(paymentId,version), HttpStatus.CREATED);
    }

}

