package com.kloia.sample.controller;

import com.kloia.eventapis.api.ViewQuery;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.sample.model.Payment;
import com.kloia.sample.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by zeldalozdemir on 09/02/2017.
 */
@Slf4j
@RestController
@RequestMapping(value = "/payment/")
@EnableFeignClients
public class PaymentRestController {

    private final ViewQuery<Payment> paymentViewQuery;
    private final PaymentRepository paymentRepository;

    public PaymentRestController(ViewQuery<Payment> paymentViewQuery, PaymentRepository paymentRepository) {
        this.paymentViewQuery = paymentViewQuery;
        this.paymentRepository = paymentRepository;
    }

    @RequestMapping(value = "/{paymentId}", method = RequestMethod.GET)
    public ResponseEntity<?> getPayment(@PathVariable("paymentId") String paymentId) {
        return new ResponseEntity<Object>(paymentRepository.findById(paymentId).get(), HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> getPayments() {
        List<Payment> payments = paymentRepository.findAll();
        return new ResponseEntity<>(payments, HttpStatus.OK);
    }

    @RequestMapping(value = "/{paymentId}/{version}", method = RequestMethod.GET)
    public ResponseEntity<?> getPaymentWithVersion(
            @PathVariable("paymentId") String paymentId, @PathVariable("version") Integer version
    ) throws Exception {
        return new ResponseEntity<Object>(paymentViewQuery.queryEntity(paymentId, version), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{paymentId}/history", method = RequestMethod.GET)
    public ResponseEntity<?> getPaymentHistory(@PathVariable("paymentId") String paymentId) throws EventStoreException {
        return new ResponseEntity<Object>(paymentViewQuery.queryHistory(paymentId), HttpStatus.OK);
    }

}

