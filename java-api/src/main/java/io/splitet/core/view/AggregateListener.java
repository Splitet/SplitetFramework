package io.splitet.core.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.splitet.core.api.EventRepository;
import io.splitet.core.api.RollbackSpec;
import io.splitet.core.api.ViewQuery;
import io.splitet.core.cassandra.EntityEvent;
import io.splitet.core.common.PublishedEvent;
import io.splitet.core.common.RecordedEvent;
import io.splitet.core.exception.EventStoreException;
import io.splitet.core.pojos.Operation;
import io.splitet.core.pojos.TransactionState;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class AggregateListener<T extends Entity> {
    final Map<String, Map.Entry<Class<RecordedEvent>, RollbackSpec>> rollbackSpecMap;
    ViewQuery<T> viewQuery;
    EventRepository eventRepository;
    SnapshotRepository<T, String> snapshotRepository;
    private ObjectMapper objectMapper;

    public AggregateListener(ViewQuery<T> viewQuery,
                             EventRepository eventRepository,
                             SnapshotRepository<T, String> snapshotRepository,
                             List<RollbackSpec> rollbackSpecs,
                             ObjectMapper objectMapper) {
        this.viewQuery = viewQuery;
        this.eventRepository = eventRepository;
        this.snapshotRepository = snapshotRepository;
        this.objectMapper = objectMapper;
        rollbackSpecMap = new HashMap<>();
        rollbackSpecs.forEach(rollbackSpec -> {
            Map.Entry<String, Class<RecordedEvent>> entry = rollbackSpec.getNameAndClass();
            rollbackSpecMap.put(entry.getKey(), new AbstractMap.SimpleEntry<>(entry.getValue(), rollbackSpec));
        });
    }

    public void listenOperations(ConsumerRecord<String, Operation> data) throws EventStoreException {
        try {
            if (data.value().getTransactionState() == TransactionState.TXN_FAILED) {
                List<EntityEvent> entityEvents = eventRepository.markFail(data.key());
                runRollbacks(entityEvents);
                snapshotRepository.saveAll(viewQuery.queryByOpId(data.key())); // We may not need this
            } else if (data.value().getTransactionState() == TransactionState.TXN_SUCCEEDED) {
                snapshotRepository.saveAll(viewQuery.queryByOpId(data.key()));
            }
            snapshotRepository.flush();
        } catch (EventStoreException e) {
            log.error("Error while applying operation:" + data.toString() + " Exception:" + e.getMessage(), e);
        }
    }

    void runRollbacks(List<EntityEvent> entityEvents) {
        entityEvents.forEach(entityEvent -> {
            try {
                Map.Entry<Class<RecordedEvent>, RollbackSpec> specEntry = rollbackSpecMap.get(entityEvent.getEventType());
                if (specEntry != null) {
                    RecordedEvent eventData = new EntityEventWrapper<>(specEntry.getKey(), objectMapper, entityEvent).getEventData();
                    if (eventData instanceof PublishedEvent) {
                        ((PublishedEvent) eventData).setSender(entityEvent.getEventKey());
                    }
                    specEntry.getValue().rollback(eventData);
                }
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        });
    }
}
