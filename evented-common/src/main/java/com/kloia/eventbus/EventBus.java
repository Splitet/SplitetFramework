package com.kloia.eventbus;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by zeldalozdemir on 26/02/2017.
 */
@Component
@Data
public class EventBus {
    @Autowired
    @Qualifier("eventsKafka")
    KafkaTemplate eventsKafkaTemplate;


}
