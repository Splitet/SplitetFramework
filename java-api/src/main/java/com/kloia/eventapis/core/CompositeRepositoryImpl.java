package com.kloia.eventapis.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        eventRecorder.recordEntityEvent(entityEvent);
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
        EntityEvent entityEvent = new EntityEvent(event.getSender(), opId, new Date(), event.getClass().getSimpleName(), EventState.CREATED, eventData);
        eventRecorder.recordEntityEvent(entityEvent);
        return entityEvent.getEventKey();
    }

/*    @Override
    public <D> EventKey recordEntityEvent(D eventData) throws EventStoreException {
        EventKey eventKey = new EventKey(idCreationStrategy.nextId(), 0);
        return recordInternal(eventData, eventKey);
    }*/

    @Override
    public List<EventKey> markFail(String opId) {
        return eventRecorder.markFail(opId);
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
    public <P extends PublishedEvent> EventKey recordAndPublish(Entity entity, P publishedEvent) throws EventStoreException {
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
