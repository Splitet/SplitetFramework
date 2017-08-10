package com.kloia.evented;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.api.impl.IOperationRepository;
import com.kloia.eventapis.api.impl.OperationContext;
import com.kloia.eventapis.pojos.EventKey;
import com.kloia.eventapis.pojos.PublishedEvent;
import com.kloia.eventapis.pojos.PublishedEventWrapper;
import com.kloia.eventapis.pojos.Views;
import com.kloia.evented.domain.EntityEvent;

import java.util.Date;

/**
 * Created by zeldalozdemir on 24/04/2017.
 */
public class EventRepositoryImpl<E extends Entity> implements EventRepository<E> {

    private IEventRepository<E> eventRepository;
    private OperationContext operationContext;
    private ObjectMapper objectMapper;
    private IOperationRepository operationRepository;
    private IUserContext userContext;
    private final static String ENTITY_EVENT_CREATED = "CREATED";
    private IdCreationStrategy idCreationStrategy = new UUIDCreationStrategy();


    public EventRepositoryImpl(IEventRepository<E> eventRepository, OperationContext operationContext, ObjectMapper objectMapper, IOperationRepository operationRepository, IUserContext userContext) {
        this.eventRepository = eventRepository;
        this.operationContext = operationContext;
        this.objectMapper = objectMapper;
        this.operationRepository = operationRepository;
        this.userContext = userContext;
    }

    public EventRepositoryImpl(IEventRepository<E> eventRepository, OperationContext operationContext, ObjectMapper objectMapper, IOperationRepository operationRepository,
                               IUserContext userContext, IdCreationStrategy idCreationStrategy) {
        this.eventRepository = eventRepository;
        this.operationContext = operationContext;
        this.objectMapper = objectMapper;
        this.operationRepository = operationRepository;
        this.userContext = userContext;
        this.idCreationStrategy = idCreationStrategy;
    }
/*
    @Override
    public <P extends PublishedOnly> void publishEvent(P publishedEvent) throws EventPulisherException {
        try {
            PublishedEventWrapper publishedEventWrapper = new PublishedEventWrapper(operationContext.getContext(), objectMapper.valueToTree(publishedEvent)); //todo add UserContext too
            publishedEventWrapper.setUserContext(userContext.getUserContext());
            operationRepository.publishEvent(publishedEvent.getClass().getSimpleName(), publishedEventWrapper);
        } catch (IOException e) {
            throw new EventPulisherException(e);
        }
    }*/

/*    @Override
    public <D> EventKey recordEntityEvent(E previousEntityState, D eventData) throws EventStoreException {
        EventKey eventKey = new EventKey(previousEntityState.getId(), previousEntityState.getVersion() + 1);
        return recordInternal( eventData, eventKey);
    }*/
/*
    private <D> EventKey recordInternal(D eventData, EventKey eventKey) throws EventStoreException {
        String opId = operationContext.getContext();
        JsonNode eventData1 = null;
        try {
            eventData1 = objectMapper.valueToTree(eventData);
        } catch (IllegalArgumentException e) {
            throw new EventStoreException(e.getMessage(), e);
        }
        EntityEvent entityEvent = new EntityEvent(eventKey, opId, new Date(), eventData.getClass().getSimpleName(), ENTITY_EVENT_CREATED, eventData1);
        eventRepository.recordEntityEvent(entityEvent);
        return eventKey;
    }*/

    private <P extends PublishedEvent> EventKey recordInternal(P event) throws EventStoreException {
        String opId = operationContext.getContext();
        String eventData = null;
        try {
            eventData = objectMapper.writerWithView(Views.RecordedOnly.class).writeValueAsString(event);
        } catch (IllegalArgumentException|JsonProcessingException e) {
            throw new EventStoreException(e.getMessage(), e);
        }
        EntityEvent entityEvent = new EntityEvent(event.getSender(), opId, new Date(), event.getClass().getSimpleName(), ENTITY_EVENT_CREATED, eventData);
        eventRepository.recordEntityEvent(entityEvent);
        return entityEvent.getEventKey();
    }

/*    @Override
    public <D> EventKey recordEntityEvent(D eventData) throws EventStoreException {
        EventKey eventKey = new EventKey(idCreationStrategy.nextId(), 0);
        return recordInternal(eventData, eventKey);
    }*/

    @Override
    public void markFail(String opId) {
        eventRepository.markFail(opId);
    }

    @Override
    public <P extends PublishedEvent> EventKey recordAndPublish(P publishedEvent) throws EventStoreException {
        EventKey eventKey = new EventKey(idCreationStrategy.nextId(), 0);
        recordAndPublishInternal(publishedEvent, eventKey);
        return eventKey;
    }

    private <P extends PublishedEvent> void recordAndPublishInternal(P publishedEvent, EventKey eventKey) throws EventStoreException {
        publishedEvent.setSender(eventKey);
        recordInternal(publishedEvent);
        String event = null;
        try {
            event = objectMapper.writerWithView(Views.PublishedOnly.class).writeValueAsString(publishedEvent);
        } catch (JsonProcessingException e) {
            throw new EventStoreException(e.getMessage(), e);
        }
        PublishedEventWrapper publishedEventWrapper = new PublishedEventWrapper(operationContext.getContext(), event);
        publishedEventWrapper.setUserContext(userContext.getUserContext());
        operationRepository.publishEvent(publishedEvent.getClass().getSimpleName(), publishedEventWrapper);
    }

    @Override
    public <P extends PublishedEvent> EventKey recordAndPublish(E entity, P publishedEvent) throws EventStoreException {
        EventKey eventKey = new EventKey(entity.getId(), entity.getVersion() + 1);
        recordAndPublishInternal(publishedEvent, eventKey);
        return eventKey;
    }

    @Override
    public <P extends PublishedEvent> EventKey recordAndPublish(EventKey eventKey, P publishedEvent) throws EventStoreException {
        EventKey newEventKey = new EventKey(eventKey.getEntityId(), eventKey.getVersion() + 1);
        recordAndPublishInternal(publishedEvent, newEventKey);
        return eventKey;
    }
}
