package io.splitet.core.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.splitet.core.api.EventRepository;
import io.splitet.core.api.Views;
import io.splitet.core.cassandra.ConcurrencyResolver;
import io.splitet.core.cassandra.ConcurrentEventException;
import io.splitet.core.cassandra.ConcurrentEventResolver;
import io.splitet.core.cassandra.DefaultConcurrencyResolver;
import io.splitet.core.cassandra.EntityEvent;
import io.splitet.core.common.EventKey;
import io.splitet.core.common.EventRecorder;
import io.splitet.core.common.EventType;
import io.splitet.core.common.PublishedEvent;
import io.splitet.core.exception.EventStoreException;
import io.splitet.core.kafka.IOperationRepository;
import io.splitet.core.pojos.EventState;
import io.splitet.core.view.Entity;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by zeldalozdemir on 24/04/2017.
 */
public class CompositeRepositoryImpl implements EventRepository {

    private EventRecorder eventRecorder;
    private ObjectMapper objectMapper;
    private IOperationRepository operationRepository;


    public CompositeRepositoryImpl(EventRecorder eventRecorder, ObjectMapper objectMapper,
                                   IOperationRepository operationRepository) {
        this.eventRecorder = eventRecorder;
        this.objectMapper = objectMapper;
        this.operationRepository = operationRepository;
    }


    @Override
    public List<EntityEvent> markFail(String opId) {
        return eventRecorder.markFail(opId);
    }

    @Override
    public <P extends PublishedEvent> EventKey recordAndPublish(P publishedEvent) throws EventStoreException, ConcurrentEventException {
        return recordAndPublishInternal(publishedEvent, Optional.empty(), entityEvent -> new DefaultConcurrencyResolver());
    }

    @Override
    public <P extends PublishedEvent> EventKey recordAndPublish(Entity previousEntity, P publishedEvent) throws EventStoreException, ConcurrentEventException {
        return recordAndPublishInternal(publishedEvent, Optional.of(previousEntity.getEventKey()), p -> new DefaultConcurrencyResolver());
    }

    @Override
    public <P extends PublishedEvent> EventKey recordAndPublish(EventKey previousEventKey, P publishedEvent) throws EventStoreException, ConcurrentEventException {
        return recordAndPublishInternal(publishedEvent, Optional.of(previousEventKey), p -> new DefaultConcurrencyResolver());
    }

    @Override
    public <P extends PublishedEvent, T extends Exception> EventKey recordAndPublish(
            Entity previousEntity, P publishedEvent, Function<EntityEvent, ConcurrencyResolver<T>> concurrencyResolverFactory
    ) throws EventStoreException, T {
        return recordAndPublishInternal(publishedEvent, Optional.of(previousEntity.getEventKey()), concurrencyResolverFactory);
    }

    @Override
    public <P extends PublishedEvent, T extends Exception> EventKey recordAndPublish(
            EventKey eventKey, P publishedEvent, Function<EntityEvent, ConcurrencyResolver<T>> concurrencyResolverFactory
    ) throws EventStoreException, T {
        return recordAndPublishInternal(publishedEvent, Optional.of(eventKey), concurrencyResolverFactory);
    }

    @Override
    public <P extends PublishedEvent, T extends Exception> EventKey recordAndPublish(
            Entity entity, P publishedEvent, Supplier<ConcurrentEventResolver<P, T>> concurrencyResolverFactory
    ) throws EventStoreException, T {
        return recordAndPublishInternal(publishedEvent, Optional.of(entity.getEventKey()), concurrencyResolverFactory);

    }

    @Override
    public <P extends PublishedEvent, T extends Exception> EventKey recordAndPublish(
            EventKey previousEventKey, P publishedEvent, Supplier<ConcurrentEventResolver<P, T>> concurrencyResolverFactory
    ) throws EventStoreException, T {
        return recordAndPublishInternal(publishedEvent, Optional.of(previousEventKey), concurrencyResolverFactory);
    }

    @Override
    public EventRecorder getEventRecorder() {
        return eventRecorder;
    }


    private <P extends PublishedEvent, T extends Exception> EventKey recordAndPublishInternal(
            P publishedEvent, Optional<EventKey> previousEventKey, Function<EntityEvent, ConcurrencyResolver<T>> concurrencyResolverFactory
    ) throws EventStoreException, T {
        long opDate = createOpDate();
        EventKey eventKey = eventRecorder.recordEntityEvent(publishedEvent, opDate, previousEventKey, concurrencyResolverFactory);
        return publishInternal(publishedEvent, opDate, eventKey);
    }

    private <P extends PublishedEvent, T extends Exception> EventKey recordAndPublishInternal(
            P publishedEvent, Optional<EventKey> previousEventKey, Supplier<ConcurrentEventResolver<P, T>> concurrencyResolverFactory
    ) throws EventStoreException, T {
        long opDate = createOpDate();
        EventKey eventKey = eventRecorder.recordEntityEvent(publishedEvent, opDate, previousEventKey, concurrencyResolverFactory);
        return publishInternal(publishedEvent, opDate, eventKey);
    }

    private long createOpDate() {
        return System.currentTimeMillis();
    }

    private <P extends PublishedEvent> EventKey publishInternal(P publishedEvent, long opDate, EventKey eventKey) throws EventStoreException {
        publishedEvent.setSender(eventKey);
        String event;
        try {
            event = objectMapper.writerWithView(Views.PublishedOnly.class).writeValueAsString(publishedEvent);
        } catch (JsonProcessingException e) {
            throw new EventStoreException(e.getMessage(), e);
        }

        operationRepository.publishEvent(publishedEvent.getClass().getSimpleName(), event, opDate);
        checkOperationFinalStates(publishedEvent);
        return publishedEvent.getSender();
    }

    private <P extends PublishedEvent> void checkOperationFinalStates(P publishedEvent) {
        if (publishedEvent.getEventType() == EventType.OP_SUCCESS || publishedEvent.getEventType() == EventType.OP_SINGLE) {
            operationRepository.successOperation(publishedEvent.getClass().getSimpleName(), successEvent -> successEvent.setEventState(EventState.TXN_SUCCEEDED));
        } else if (publishedEvent.getEventType() == EventType.OP_FAIL) {
            operationRepository.failOperation(publishedEvent.getClass().getSimpleName(), failEvent -> failEvent.setEventState(EventState.TXN_FAILED));
        }
    }
}
