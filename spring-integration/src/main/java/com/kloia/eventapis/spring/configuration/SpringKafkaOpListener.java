package com.kloia.eventapis.spring.configuration;

import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.view.AggregateListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Controller
@Slf4j
public class SpringKafkaOpListener {
    @Autowired(required = false)
    List<AggregateListener> aggregateListeners;

    @Transactional
    @KafkaListener(id = "op-listener", topics = Operation.OPERATION_EVENTS, containerFactory = "operationsKafkaListenerContainerFactory")
    void listenOperations(ConsumerRecord<String, Operation> record) throws EventStoreException {
        String key = record.key();
        Operation value = record.value();
        log.info("Incoming Message: " + key + " " + value);
        for (AggregateListener snapshotRecorder : aggregateListeners) {
            snapshotRecorder.listenOperations(record);
        }
        if (value != null)
            throw new RuntimeException("Bla bla");
    }
    public void recover(Exception e) throws Exception {
        log.error("Operation Handle is failed");
        throw e;
    }
}
