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
import com.kloia.eventapis.common.PublishableEvent;
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
    private OperationContext operationContext;
    private ObjectMapper objectMapper;
    private IOperationRepository operationRepository;
    private IUserContext userContext;


    public CompositeRepositoryImpl(EventRecorder eventRecorder, OperationContext operationContext, ObjectMapper objectMapper,
                                   IOperationRepository operationRepository, IUserContext userContext) {
        this.eventRecorder = eventRecorder;
        this.operationContext = operationContext;
        this.objectMapper = objectMapper;
        this.operationRepository = operationRepository;
        this.userContext = userContext;
    }


    @Override
    public List<EntityEvent> markFail(String opId) {
        return eventRecorder.markFail(opId);
    }

    @Override
    public <P extends PublishableEvent> EventKey recordAndPublish(P publishableEvent) throws EventStoreException, ConcurrentEventException {
        return recordAndPublishInternal(publishableEvent, Optional.empty(), entityEvent -> new DefaultConcurrencyResolver());
    }

    @Override
    public <P extends PublishableEvent> EventKey recordAndPublish(Entity previousEntity, P publishableEvent) throws EventStoreException, ConcurrentEventException {
        return recordAndPublishInternal(publishableEvent, Optional.of(previousEntity.getEventKey()), p -> new DefaultConcurrencyResolver());
    }

    @Override
    public <P extends PublishableEvent> EventKey recordAndPublish(EventKey previousEventKey, P publishableEvent) throws EventStoreException, ConcurrentEventException {
        return recordAndPublishInternal(publishableEvent, Optional.of(previousEventKey), p -> new DefaultConcurrencyResolver());
    }

    @Override
    public <P extends PublishableEvent, T extends Exception> EventKey recordAndPublish(
            Entity entity, P publishableEvent, Function<EntityEvent, ConcurrencyResolver<T>> concurrencyResolverFactory
    ) throws EventStoreException, T {
        return recordAndPublishInternal(publishableEvent, Optional.of(entity.getEventKey()), concurrencyResolverFactory);
    }

    @Override
    public <P extends PublishableEvent, T extends Exception> EventKey recordAndPublish(
            EventKey previousEventKey, P publishableEvent, Function<EntityEvent, ConcurrencyResolver<T>> concurrencyResolverFactory
    ) throws EventStoreException, T {
        return recordAndPublishInternal(publishableEvent, Optional.of(previousEventKey), concurrencyResolverFactory);
    }


    private <P extends PublishableEvent, T extends Exception> EventKey recordAndPublishInternal(
            P publishableEvent, Optional<EventKey> previousEventKey, Function<EntityEvent, ConcurrencyResolver<T>> concurrencyResolverFactory
    ) throws EventStoreException, T {
        long opDate = System.currentTimeMillis();
        EventKey eventKey = eventRecorder.recordEntityEvent(publishableEvent, opDate, previousEventKey, concurrencyResolverFactory);
        publishableEvent.setSender(eventKey);
        String event;
        try {
            event = objectMapper.writerWithView(Views.PublishedOnly.class).writeValueAsString(publishableEvent);
        } catch (JsonProcessingException e) {
            throw new EventStoreException(e.getMessage(), e);
        }

        PublishedEventWrapper publishableEventWrapper = new PublishedEventWrapper(operationContext.getContext(), operationContext.getCommandContext(), event, opDate);
        publishableEventWrapper.setUserContext(userContext.getUserContext());
        operationRepository.publishEvent(publishableEvent.getClass().getSimpleName(), publishableEventWrapper);
        checkOperationFinalStates(publishableEvent);
        return publishableEvent.getSender();
    }

    private <P extends PublishableEvent> void checkOperationFinalStates(P publishableEvent) {
        if (publishableEvent.getEventType() == EventType.OP_SUCCESS || publishableEvent.getEventType() == EventType.OP_SINGLE) {
            operationRepository.successOperation(operationContext.getContext(), publishableEvent.getClass().getSimpleName(), successEvent -> successEvent.setEventState(EventState.TXN_SUCCEDEED));
        } else if (publishableEvent.getEventType() == EventType.OP_FAIL) {
            operationRepository.failOperation(operationContext.getContext(), publishableEvent.getClass().getSimpleName(), failEvent -> failEvent.setEventState(EventState.TXN_FAILED));
        }
    }
}
