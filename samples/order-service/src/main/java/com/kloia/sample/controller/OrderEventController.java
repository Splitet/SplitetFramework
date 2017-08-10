package com.kloia.sample.controller;

import com.kloia.evented.EventPulisherException;
import com.kloia.evented.EventStoreException;
import com.kloia.sample.commands.OrderPaidCommand;
import com.kloia.sample.commands.ProcessPaymentCommand;
import com.kloia.sample.dto.command.ProcessOrderPaymentCommandDto;
import com.kloia.sample.dto.event.OrderCreatedEvent;
import com.kloia.sample.dto.event.PaymentSuccessEvent;
import com.kloia.sample.dto.event.StockReservedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Controller;


/**
 * Created by zeldalozdemir on 09/02/2017.
 */
@Slf4j
@Controller
public class OrderEventController {

    @Autowired
    ProcessPaymentCommand processPaymentCommand;
    @Autowired
    OrderPaidCommand orderPaidCommand;

    @KafkaListener(topics = "OrderCreatedEvent", containerFactory = "eventsKafkaListenerContainerFactory")
    public void orderCreatedEvent(OrderCreatedEvent event) throws  EventPulisherException, EventStoreException {
        log.info("OrderCreatedEvent: "+event);
    }
    @KafkaListener(topics = "StockReservedEvent", containerFactory = "eventsKafkaListenerContainerFactory")
    public void stockReservedEvent(StockReservedEvent event) throws  EventPulisherException, EventStoreException {
        log.info("OrderCreatedEvent: "+event);
        processPaymentCommand.execute(event);
    }

    @KafkaListener(topics = "PaymentSuccessEvent", containerFactory = "eventsKafkaListenerContainerFactory")
    public void paymentSuccessEvent(PaymentSuccessEvent event) throws  EventPulisherException, EventStoreException {
        log.info("paymentSuccessEvent: "+event);
        orderPaidCommand.execute(event);
    }

}

