package com.kloia.eventapis.view;

import com.kloia.eventapis.api.EventRepository;
import com.kloia.eventapis.api.ViewQuery;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.pojos.TransactionState;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@Slf4j
public class SnapshotRecorder {
    ViewQuery viewQuery;
    EventRepository eventRepository;
    SnapshotRepository snapshotRepository;

    public SnapshotRecorder(ViewQuery viewQuery, EventRepository eventRepository, SnapshotRepository snapshotRepository) {
        this.viewQuery = viewQuery;
        this.eventRepository = eventRepository;
        this.snapshotRepository = snapshotRepository;
    }

    public void listenOperations(ConsumerRecord<String, Operation> data) throws EventStoreException {
        log.info("Incoming Message: " + data.value());
        if (data.value().getTransactionState() == TransactionState.TXN_FAILED) {
            eventRepository.markFail(data.key());
            snapshotRepository.save(viewQuery.queryByOpId(data.key()));
        }else if (data.value().getTransactionState() == TransactionState.TXN_SUCCEDEED) {
            snapshotRepository.save(viewQuery.queryByOpId(data.key()));
        }
    }
}
