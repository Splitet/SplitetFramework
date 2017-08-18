package com.kloia.eventapis.view;

import com.kloia.eventapis.api.EventRepository;
import com.kloia.eventapis.api.ViewQuery;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.pojos.TransactionState;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.List;

@Slf4j
public class AggregateListener {
    ViewQuery viewQuery;
    EventRepository eventRepository;
    SnapshotRepository snapshotRepository;

    public AggregateListener(ViewQuery viewQuery, EventRepository eventRepository, SnapshotRepository snapshotRepository, List<RollbackSpec> rollbackSpecs) {
        this.viewQuery = viewQuery;
        this.eventRepository = eventRepository;
        this.snapshotRepository = snapshotRepository;
    }

    public void listenOperations(ConsumerRecord<String, Operation> data) {
        try {
            log.info("Incoming Message: " + data.key()+ " "+ data.value());
            if (data.value().getTransactionState() == TransactionState.TXN_FAILED) {
                eventRepository.markFail(data.key());
                snapshotRepository.save(viewQuery.queryByOpId(data.key()));
            }else if (data.value().getTransactionState() == TransactionState.TXN_SUCCEDEED) {
                snapshotRepository.save(viewQuery.queryByOpId(data.key()));
            }
        } catch (EventStoreException e) {
            log.error("Error while applying operation:"+data.toString()+" Exception:"+e.getMessage(),e);
        }
    }
}
