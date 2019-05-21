package com.kloia.eventapis.spring.configuration;

import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.view.AggregateListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class AggregateListenerService {
    @Autowired(required = false)
    List<AggregateListener> aggregateListeners;


    @Transactional(rollbackFor = Exception.class)
    public void listenOperations(ConsumerRecord<String, Operation> record) throws EventStoreException {
        for (AggregateListener snapshotRecorder : aggregateListeners) {
            snapshotRecorder.listenOperations(record);
        }
    }
}
