package com.kloia.eventapis.spring.configuration;

import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.view.SnapshotRecorder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class SpringKafkaOpListener {
    @Autowired
    List<SnapshotRecorder> snapshotRecorders;

    @KafkaListener(id = "op-listener", topics = "operation-events", containerFactory = "operationsKafkaListenerContainerFactory")
    private void listenOperations(ConsumerRecord<String, Operation> data) throws EventStoreException {
        for (SnapshotRecorder snapshotRecorder : snapshotRecorders) {
            snapshotRecorder.listenOperations(data);
        }
    }
}
