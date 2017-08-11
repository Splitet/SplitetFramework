package com.kloia.sample.controller;

import com.kloia.eventapis.exception.EventPulisherException;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.sample.controller.event.DoPaymentEventHandler;
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



}

