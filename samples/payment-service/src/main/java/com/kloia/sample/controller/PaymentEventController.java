package com.kloia.sample.controller;

import com.kloia.evented.EventPulisherException;
import com.kloia.evented.EventStoreException;
import com.kloia.sample.commands.DoPaymentCommand;
import com.kloia.sample.dto.command.PaymentProcessDto;
import com.kloia.sample.dto.event.PaymentProcessEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Controller;


/**
 * Created by zeldalozdemir on 09/02/2017.
 */
@Slf4j
@Controller
public class PaymentEventController {
    @Autowired
    private DoPaymentCommand doPaymentCommand;

    @KafkaListener(topics = "PaymentProcessEvent", containerFactory = "eventsKafkaListenerContainerFactory")
    public void doPaymentDto(PaymentProcessEvent event) throws  EventPulisherException, EventStoreException {
        log.info("PaymentProcessEvent: "+event);
        doPaymentCommand.execute(event);
    }


}

