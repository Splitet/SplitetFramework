package com.kloia.sample;

import com.kloia.evented.EventStoreException;
import com.kloia.evented.IEventRepository;
import com.kloia.sample.commands.CreatePaymentCommand;
import com.kloia.sample.dto.Payment;
import com.kloia.sample.dto.PaymentProcessAggDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.io.IOException;

/**
 * Created by zeldalozdemir on 09/02/2017.
 */
@Slf4j
@RequestMapping(value = "/aggr/v1/payment/")
@SpringBootApplication
public class TestPaymentService {

    @Autowired
    @Qualifier("orderEventRepository")
    private IEventRepository<Payment> paymentEventRepository;


    @Autowired
    CreatePaymentCommand createPaymentCommand;



    @PostConstruct
    public void init() {
        paymentEventRepository.addAggregateSpecs(createPaymentCommand);
    }

    @RequestMapping(value = "/{paymentId}", method = RequestMethod.GET)
    public ResponseEntity<?> getPayment(@PathVariable("paymentId") Long paymentId) throws IOException, EventStoreException {

        Payment payment = paymentEventRepository.queryEntity(paymentId);
        if (payment == null)
            return new ResponseEntity<Object>("No Such Payment:" + paymentId, HttpStatus.NOT_FOUND);
        return new ResponseEntity<Object>(payment, HttpStatus.OK);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity<?> aggregateCreateOrder(@RequestBody @Valid PaymentProcessAggDTO paymentProcessAggDTO) throws IOException, EventStoreException, InterruptedException {
//        TemplateAccount saved = createTemplateAccountService.create(orderCreateDTO);
        log.info("Template account saved: " + paymentProcessAggDTO);
        createPaymentCommand.processCommand(paymentProcessAggDTO);
        if (paymentProcessAggDTO.getPaymentId() % 2 == 1)
            throw new IOException("We failed:////");
        else
            return new ResponseEntity<Object>(paymentEventRepository.queryEntity(paymentProcessAggDTO.getPaymentId()), HttpStatus.CREATED);
    }
}
