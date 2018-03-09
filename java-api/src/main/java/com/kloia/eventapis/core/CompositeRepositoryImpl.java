package com.kloia.eventapis.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.api.EventRepository;
import com.kloia.eventapis.api.IUserContext;
import com.kloia.eventapis.api.Views;
import com.kloia.eventapis.cassandra.ConcurrencyResolver;
import com.kloia.eventapis.cassandra.ConcurrentEventException;
import com.kloia.eventapis.cassandra.DefaultConcurrencyResolver;
import com.kloia.eventapis.cassandra.EntityEvent;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.common.EventRecorder;
import com.kloia.eventapis.common.EventType;
import com.kloia.eventapis.common.OperationContext;
import com.kloia.eventapis.common.PublishedEvent;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.kafka.IOperationRepository;
import com.kloia.eventapis.kafka.PublishedEventWrapper;
import com.kloia.eventapis.pojos.EventState;
import com.kloia.eventapis.view.Entity;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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
            Entity entity, P publishedEvent, Function<EntityEvent, ConcurrencyResolver<T>> concurrencyResolverFactory
    ) throws EventStoreException, T {
        return recordAndPublishInternal(publishedEvent, Optional.of(entity.getEventKey()), concurrencyResolverFactory);
    }

    @Override
    public <P extends PublishedEvent, T extends Exception> EventKey recordAndPublish(
            EventKey previousEventKey, P publishedEvent, Function<EntityEvent, ConcurrencyResolver<T>> concurrencyResolverFactory
    ) throws EventStoreException, T {
        return recordAndPublishInternal(publishedEvent, Optional.of(previousEventKey), concurrencyResolverFactory);
    }


    private <P extends PublishedEvent, T extends Exception> EventKey recordAndPublishInternal(
            P publishedEvent, Optional<EventKey> previousEventKey, Function<EntityEvent, ConcurrencyResolver<T>> concurrencyResolverFactory
    ) throws EventStoreException, T {
        long opDate = System.currentTimeMillis();
        EventKey eventKey = eventRecorder.recordEntityEvent(publishedEvent, opDate, previousEventKey, concurrencyResolverFactory);
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
            operationRepository.successOperation( publishedEvent.getClass().getSimpleName(), successEvent -> successEvent.setEventState(EventState.TXN_SUCCEDEED));
        } else if (publishedEvent.getEventType() == EventType.OP_FAIL) {
            operationRepository.failOperation( publishedEvent.getClass().getSimpleName(), failEvent -> failEvent.setEventState(EventState.TXN_FAILED));
        }
    }
}
