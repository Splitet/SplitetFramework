package com.kloia.evented;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.api.impl.KafkaOperationRepository;
import com.kloia.eventapis.api.impl.OperationContext;
import com.kloia.evented.domain.EntityEvent;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 24/04/2017.
 */
@Service
public class EventRepositoryImpl<E extends Entity> implements EventRepository<E> {

    private CassandraEventRepository<E> cassandraEventRepository;
    private OperationContext operationContext;
    private ObjectMapper  objectMapper;
    private KafkaOperationRepository kafka;
    private final static String ENTITY_EVENT_CREATED = "CREATED";



    public EventRepositoryImpl(CassandraEventRepository<E> cassandraEventRepository, OperationContext operationContext, ObjectMapper objectMapper, KafkaOperationRepository kafka) {
        this.cassandraEventRepository = cassandraEventRepository;
        this.operationContext = operationContext;
        this.objectMapper = objectMapper;
        this.kafka = kafka;
    }

    @Override
    public void publishEvent(Event event) {
        kafka.publishEvent(event.getClass().getSimpleName(),event);
    }

    @Override
    public void addAggregateSpecs(List<EntityFunctionSpec<E, ?>> commandSpec) {
        cassandraEventRepository.addAggregateSpecs(commandSpec);
    }

    @Override
    public <D extends Serializable> EventKey recordEntityEvent(E previousEntityState, Class<? extends EntityFunctionSpec<E, D>> entitySpecClass, D eventData) throws EventStoreException {
        EventKey eventKey = new EventKey(previousEntityState.getId(), previousEntityState.getVersion() + 1);
        return recordInternal(entitySpecClass, eventData, eventKey);
    }

    private <D extends Serializable> EventKey recordInternal(Class<? extends EntityFunctionSpec<E, D>> entitySpecClass, D eventData, EventKey eventKey) throws EventStoreException {
        UUID opId = operationContext.getContext();
        String eventData1 = null;
        try {
            eventData1 = objectMapper.writer().writeValueAsString(eventData);
        } catch (JsonProcessingException e) {
            throw new EventStoreException(e.getMessage(),e);
        }
        EntityEvent entityEvent = new EntityEvent(eventKey, opId,new Date(), eventData.getClass().getName(),ENTITY_EVENT_CREATED, eventData1);
        cassandraEventRepository.recordEntityEvent(entityEvent);
        return eventKey;
    }

    @Override
    public <D extends Serializable> EventKey recordEntityEvent(Class<? extends EntityFunctionSpec<E, D>> entitySpecClass, D eventData) throws EventStoreException {
        EventKey eventKey = new EventKey(UUID.randomUUID(),0); // todo sequence or random
        return recordInternal(entitySpecClass, eventData, eventKey);
    }
}
