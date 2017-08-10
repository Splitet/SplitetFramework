package com.kloia.sample.controller;

import com.kloia.eventapis.common.EventKey;
import com.kloia.sample.commands.ReserveStockCommand;
import com.kloia.sample.dto.event.ReserveStockEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Controller;


/**
 * Created by zeldalozdemir on 09/02/2017.
 */
@Slf4j
@Controller
public class StockEventController {

    @Autowired
    private ReserveStockCommand reserveStockCommand;

    @KafkaListener(topics = "ReserveStockEvent", containerFactory = "eventsKafkaListenerContainerFactory")
    public void orderCreatedEvent(ReserveStockEvent event) throws Exception {
        EventKey execute = reserveStockCommand.execute(event);
    }

}

