package com.kloia.eventapis.spring.configuration;

import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.view.AggregateListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@Slf4j
public class SpringKafkaOpListener {
    @Autowired(required = false)
    List<AggregateListener> aggregateListeners;

    @KafkaListener(id = "op-listener", topics = "operation-events", containerFactory = "operationsKafkaListenerContainerFactory")
    private void listenOperations(ConsumerRecord<String, Operation> data) throws EventStoreException {
        log.info("Incoming Message: " + data.key()+ " "+ data.value());
        for (AggregateListener snapshotRecorder : aggregateListeners) {
            snapshotRecorder.listenOperations(data);
        }
    }
}
