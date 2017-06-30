package com.kloia.evented;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.api.impl.KafkaOperationRepository;
import com.kloia.eventapis.api.impl.OperationContext;
import com.kloia.eventapis.pojos.PublishedEvent;
import com.kloia.eventapis.pojos.PublishedEventWrapper;
import com.kloia.evented.domain.EntityEvent;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 24/04/2017.
 */
public class EventRepositoryImpl<E extends Entity> implements EventRepository<E> {

    private IEventRepository<E> eventRepository;
    private OperationContext operationContext;
    private ObjectMapper objectMapper;
    private KafkaOperationRepository kafka;
    private IUserContext userContext;
    private final static String ENTITY_EVENT_CREATED = "CREATED";
    private IdCreationStrategy idCreationStrategy = new UUIDCreationStrategy();


    public EventRepositoryImpl(IEventRepository<E> eventRepository, OperationContext operationContext, ObjectMapper objectMapper, KafkaOperationRepository kafka, IUserContext userContext) {
        this.eventRepository = eventRepository;
        this.operationContext = operationContext;
        this.objectMapper = objectMapper;
        this.kafka = kafka;
        this.userContext = userContext;
    }

    public EventRepositoryImpl(IEventRepository<E> eventRepository, OperationContext operationContext, ObjectMapper objectMapper, KafkaOperationRepository kafka,
                               IUserContext userContext, IdCreationStrategy idCreationStrategy) {
        this.eventRepository = eventRepository;
        this.operationContext = operationContext;
        this.objectMapper = objectMapper;
        this.kafka = kafka;
        this.userContext = userContext;
        this.idCreationStrategy = idCreationStrategy;
    }

    @Override
    public <P extends PublishedEvent> void publishEvent(P publishedEvent) throws EventPulisherException {
        try {
            PublishedEventWrapper publishedEventWrapper = new PublishedEventWrapper(operationContext.getContext(), objectMapper.valueToTree(publishedEvent)); //todo add UserContext too
            publishedEventWrapper.setUserContext(userContext.getUserContext());
            kafka.publishEvent(publishedEvent.getClass().getSimpleName(), publishedEventWrapper);
        } catch (IOException e) {
            throw new EventPulisherException(e);
        }
    }

    @Override
    public <D extends Serializable> EventKey recordEntityEvent(E previousEntityState, Class<? extends EntityFunctionSpec<E, D>> entitySpecClass, D eventData) throws EventStoreException {
        EventKey eventKey = new EventKey(previousEntityState.getId(), previousEntityState.getVersion() + 1);
        return recordInternal(entitySpecClass, eventData, eventKey);
    }

    private <D extends Serializable> EventKey recordInternal(Class<? extends EntityFunctionSpec<E, D>> entitySpecClass, D eventData, EventKey eventKey) throws EventStoreException {
        UUID opId = operationContext.getContext();
        JsonNode eventData1 = null;
        try {
            eventData1 = objectMapper.valueToTree(eventData);
        } catch (IllegalArgumentException e) {
            throw new EventStoreException(e.getMessage(), e);
        }
        EntityEvent entityEvent = new EntityEvent(eventKey, opId, new Date(), entitySpecClass.getSimpleName(), ENTITY_EVENT_CREATED, eventData1);
        eventRepository.recordEntityEvent(entityEvent);
        return eventKey;
    }

    @Override
    public <D extends Serializable> EventKey recordEntityEvent(Class<? extends EntityFunctionSpec<E, D>> entitySpecClass, D eventData) throws EventStoreException {
        EventKey eventKey = new EventKey(idCreationStrategy.nextId(), 0);
        return recordInternal(entitySpecClass, eventData, eventKey);
    }

    @Override
    public void markFail(UUID opId) {
        eventRepository.markFail(opId);
    }
}
