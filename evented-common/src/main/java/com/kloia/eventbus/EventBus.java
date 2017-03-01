package com.kloia.eventbus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by zeldalozdemir on 26/02/2017.
 */
@Component
public class EventBus {
    @Autowired
    KafkaTemplate kafkaTemplate;


}
