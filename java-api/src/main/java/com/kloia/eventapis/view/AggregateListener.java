package com.kloia.eventapis.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeToken;
import com.kloia.eventapis.api.EventRepository;
import com.kloia.eventapis.api.RollbackSpec;
import com.kloia.eventapis.api.ViewQuery;
import com.kloia.eventapis.cassandra.EntityEvent;
import com.kloia.eventapis.common.ReceivedEvent;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.pojos.TransactionState;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.lang.reflect.ParameterizedType;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class AggregateListener<T extends Entity> {
    private final Map<String, Map.Entry<Class<ReceivedEvent>, RollbackSpec>> rollbackSpecMap;
    private ViewQuery<T> viewQuery;
    private EventRepository eventRepository;
    private SnapshotRepository<T, String> snapshotRepository;
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
            ParameterizedType type = (ParameterizedType) TypeToken.of(rollbackSpec.getClass()).getSupertype(RollbackSpec.class).getType();
            try {
                Class<ReceivedEvent> publishedEventClass = (Class<ReceivedEvent>) Class.forName(type.getActualTypeArguments()[0].getTypeName());
                rollbackSpecMap.put(publishedEventClass.getSimpleName(), new AbstractMap.SimpleEntry<>(publishedEventClass, rollbackSpec));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void listenOperations(ConsumerRecord<String, Operation> data) {
        try {
            if (data.value().getTransactionState() == TransactionState.TXN_FAILED) {
                List<EntityEvent> entityEvents = eventRepository.markFail(data.key());
                entityEvents.forEach(entityEvent -> {
                    try {
                        Map.Entry<Class<ReceivedEvent>, RollbackSpec> specEntry = rollbackSpecMap.get(entityEvent.getEventType());
                        if (specEntry != null)
                            specEntry.getValue().rollback(new EntityEventWrapper<>(specEntry.getKey(), objectMapper, entityEvent).getEventData());
                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                    }
                });
//                List<T> list = viewQuery.queryByOpId(data.key(), o -> snapshotRepository.findOne(o));
//                snapshotRepository.save(list);
                snapshotRepository.save(viewQuery.queryByOpId(data.key())); // We may not need this
            } else if (data.value().getTransactionState() == TransactionState.TXN_SUCCEDEED) {
//                List<T> list = viewQuery.queryByOpId(data.key(), o -> snapshotRepository.findOne(o));
//                snapshotRepository.save(list);
                snapshotRepository.save(viewQuery.queryByOpId(data.key()));
            }
        } catch (EventStoreException e) {
            log.error("Error while applying operation:" + data.toString() + " Exception:" + e.getMessage(), e);
        }
    }
}
