package com.kloia.eventapis.spring.configuration;

import com.kloia.eventapis.api.IUserContext;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.view.AggregateListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Controller
@Slf4j
public class SpringKafkaOpListener {
    @Autowired(required = false)
    List<AggregateListener> aggregateListeners;

    @Autowired
    IUserContext userContext;


    @Transactional(rollbackFor = Exception.class)
    @KafkaListener(id = "op-listener", topics = Operation.OPERATION_EVENTS, containerFactory = "operationsKafkaListenerContainerFactory")
    void listenOperations(ConsumerRecord<String, Operation> record) {
        String key = record.key();
        Operation value = record.value();
        log.debug("Incoming Message: " + key + " " + value);
        userContext.extractUserContext(value.getUserContext());
        for (AggregateListener snapshotRecorder : aggregateListeners) {
            snapshotRecorder.listenOperations(record);
        }
    }

    public void recover(Exception exception) throws Exception {
        log.error("Operation Handle is failed");
        throw exception;
    }
}
