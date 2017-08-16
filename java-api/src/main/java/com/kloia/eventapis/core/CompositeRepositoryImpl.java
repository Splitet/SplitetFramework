package com.kloia.eventapis.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.cassandra.ConcurrencyResolver;
import com.kloia.eventapis.cassandra.ConcurrentEventException;
import com.kloia.eventapis.cassandra.DefaultConcurrencyResolver;
import com.kloia.eventapis.common.EventRecorder;
import com.kloia.eventapis.api.EventRepository;
import com.kloia.eventapis.api.IUserContext;
import com.kloia.eventapis.api.IdCreationStrategy;
import com.kloia.eventapis.api.impl.UUIDCreationStrategy;
import com.kloia.eventapis.common.EventType;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.kafka.IOperationRepository;
import com.kloia.eventapis.common.OperationContext;
import com.kloia.eventapis.api.Views;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.common.PublishedEvent;
import com.kloia.eventapis.kafka.PublishedEventWrapper;
import com.kloia.eventapis.cassandra.EntityEvent;
import com.kloia.eventapis.pojos.EventState;
import com.kloia.eventapis.view.Entity;

import java.util.Date;
import java.util.List;
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
    private IdCreationStrategy idCreationStrategy = new UUIDCreationStrategy();


    public CompositeRepositoryImpl(EventRecorder eventRecorder, OperationContext operationContext, ObjectMapper objectMapper, IOperationRepository operationRepository, IUserContext userContext) {
        this.eventRecorder = eventRecorder;
        this.operationContext = operationContext;
        this.objectMapper = objectMapper;
        this.operationRepository = operationRepository;
        this.userContext = userContext;
    }

    public CompositeRepositoryImpl(EventRecorder eventRecorder, OperationContext operationContext, ObjectMapper objectMapper, IOperationRepository operationRepository,
                                   IUserContext userContext, IdCreationStrategy idCreationStrategy) {
        this.eventRecorder = eventRecorder;
        this.operationContext = operationContext;
        this.objectMapper = objectMapper;
        this.operationRepository = operationRepository;
        this.userContext = userContext;
        this.idCreationStrategy = idCreationStrategy;
    }

    private <P extends PublishedEvent> EventKey recordInternal(P event, Function<EntityEvent, ConcurrencyResolver> concurrencyResolverFactory) throws EventStoreException, ConcurrentEventException {
        String opId = operationContext.getContext();
        String eventData = null;
        try {
            eventData = objectMapper.writerWithView(Views.RecordedOnly.class).writeValueAsString(event);
        } catch (IllegalArgumentException|JsonProcessingException e) {
            throw new EventStoreException(e.getMessage(), e);
        }
        EntityEvent entityEvent = new EntityEvent(event.getSender(), opId, new Date(), event.getClass().getSimpleName(), EventState.CREATED, userContext.getAuditInfo(), eventData);
        eventRecorder.recordEntityEvent(entityEvent, concurrencyResolverFactory);
        return entityEvent.getEventKey();
    }

    @Override
    public List<EventKey> markFail(String opId) {
        return eventRecorder.markFail(opId);
    }

    private <P extends PublishedEvent> void recordAndPublishInternal(P publishedEvent, EventKey eventKey,Function<EntityEvent, ConcurrencyResolver> concurrencyResolverFactory) throws EventStoreException, ConcurrentEventException {
        publishedEvent.setSender(eventKey);
        recordInternal(publishedEvent, concurrencyResolverFactory);
        String event = null;
        try {
            event = objectMapper.writerWithView(Views.PublishedOnly.class).writeValueAsString(publishedEvent);
        } catch (JsonProcessingException e) {
            throw new EventStoreException(e.getMessage(), e);
        }
        PublishedEventWrapper publishedEventWrapper = new PublishedEventWrapper(operationContext.getContext(), event);
        publishedEventWrapper.setUserContext(userContext.getUserContext());
        operationRepository.publishEvent(publishedEvent.getClass().getSimpleName(), publishedEventWrapper);
        checkOperationFinalStates(publishedEvent);
    }

    private <P extends PublishedEvent> void checkOperationFinalStates(P publishedEvent) {
        if(publishedEvent.getEventType() == EventType.OP_SUCCESS || publishedEvent.getEventType() == EventType.OP_SINGLE ){
            operationRepository.successOperation(operationContext.getContext(),operationContext.getCommandContext(),successEvent -> successEvent.setEventState(EventState.TXN_SUCCEDEED));
        }else if(publishedEvent.getEventType() == EventType.OP_FAIL){
            operationRepository.failOperation(operationContext.getContext(),operationContext.getCommandContext(),failEvent -> failEvent.setEventState(EventState.TXN_FAILED));
        }
    }

    @Override
    public <P extends PublishedEvent> EventKey recordAndPublish(P publishedEvent) throws EventStoreException, ConcurrentEventException {
        EventKey eventKey = new EventKey(idCreationStrategy.nextId(), 0);
        recordAndPublishInternal(publishedEvent, eventKey,entityEvent -> new DefaultConcurrencyResolver());
        return eventKey;
    }

    @Override
    public <P extends PublishedEvent> EventKey recordAndPublish(Entity entity, P publishedEvent) throws EventStoreException, ConcurrentEventException {
        EventKey eventKey = new EventKey(entity.getId(), entity.getVersion() + 1);
        recordAndPublishInternal(publishedEvent, eventKey, p -> new DefaultConcurrencyResolver());
        return eventKey;
    }

    @Override
    public <P extends PublishedEvent> EventKey recordAndPublish(Entity entity, P publishedEvent, Function<EntityEvent, ConcurrencyResolver> concurrencyResolverFactory) throws EventStoreException, ConcurrentEventException {
        EventKey eventKey = new EventKey(entity.getId(), entity.getVersion() + 1);
        recordAndPublishInternal(publishedEvent, eventKey, concurrencyResolverFactory);
        return eventKey;
    }

    @Override
    public <P extends PublishedEvent> EventKey recordAndPublish(EventKey eventKey, P publishedEvent) throws EventStoreException, ConcurrentEventException {
        EventKey newEventKey = new EventKey(eventKey.getEntityId(), eventKey.getVersion() + 1);
        recordAndPublishInternal(publishedEvent, newEventKey, p -> new DefaultConcurrencyResolver());
        return eventKey;
    }

    @Override
    public <P extends PublishedEvent> EventKey recordAndPublish(EventKey eventKey, P publishedEvent, Function<EntityEvent, ConcurrencyResolver> concurrencyResolverFactory) throws EventStoreException, ConcurrentEventException {
        EventKey newEventKey = new EventKey(eventKey.getEntityId(), eventKey.getVersion() + 1);
        recordAndPublishInternal(publishedEvent, newEventKey,  concurrencyResolverFactory);
        return eventKey;
    }
}
