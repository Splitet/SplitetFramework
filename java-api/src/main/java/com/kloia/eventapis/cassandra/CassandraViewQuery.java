package com.kloia.eventapis.cassandra;

import com.datastax.driver.core.PagingIterable;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.api.ViewQuery;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.common.PublishedEvent;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.pojos.EventState;
import com.kloia.eventapis.view.Entity;
import com.kloia.eventapis.view.EntityEventWrapper;
import com.kloia.eventapis.view.EntityFunction;
import com.kloia.eventapis.view.EntityFunctionSpec;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by zeldalozdemir on 12/02/2017.
 */
@Slf4j
public class CassandraViewQuery<E extends Entity> implements ViewQuery<E> {

    private String tableName;
    private String tableNameByOps;
    private CassandraSession cassandraSession;
    private Map<String, EntityFunctionSpec<E, ?>> functionMap = new HashMap<>();
    private ObjectMapper objectMapper;
    private Class<E> entityType;

    public CassandraViewQuery(String tableName, CassandraSession cassandraSession, ObjectMapper objectMapper, List<EntityFunctionSpec<E, ?>> commandSpecs) {
        this.tableName = tableName;
        this.tableNameByOps = tableName + "_byOps";
        this.cassandraSession = cassandraSession;
        this.objectMapper = objectMapper;
        addCommandSpecs(commandSpecs);
    }

    static EntityEvent convertToEntityEvent(Row entityEventData) {
        EventKey eventKey = new EventKey(entityEventData.getString(CassandraEventRecorder.ENTITY_ID), entityEventData.getInt(CassandraEventRecorder.VERSION));
        String opId = entityEventData.getString(CassandraEventRecorder.OP_ID);
        String eventData = entityEventData.getString(CassandraEventRecorder.EVENT_DATA);
        return new EntityEvent(eventKey, opId,
                entityEventData.getTimestamp(CassandraEventRecorder.OP_DATE),
                entityEventData.getString(CassandraEventRecorder.EVENT_TYPE),
                EventState.valueOf(entityEventData.getString(CassandraEventRecorder.STATUS)),
                entityEventData.getString(CassandraEventRecorder.AUDIT_INFO),
                eventData);
    }

    @Override
    public E queryEntity(String entityId) throws EventStoreException {
        Select select = QueryBuilder.select().from(tableName);
        select.where(QueryBuilder.eq(CassandraEventRecorder.ENTITY_ID, entityId));
        return queryEntityInternal(entityId, select);
    }

    private E queryEntityInternal(String entityId, Select select) throws EventStoreException {

        E initialInstance, result = null;
        try {
            initialInstance = entityType.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            log.error(e.getMessage(), e);
            throw new EventStoreException(e);
        }

        List<Row> entityEventDatas = cassandraSession.execute(select, PagingIterable::all);
        for (Row entityEventData : entityEventDatas) {
            EntityEvent entityEvent = convertToEntityEvent(entityEventData);
            if (entityEvent.getStatus() == EventState.CREATED || entityEvent.getStatus() == EventState.SUCCEDEED) {
                EntityFunctionSpec<E, ?> functionSpec = functionMap.get(entityEvent.getEventType());
                if (functionSpec != null) {
                    EntityEventWrapper eventWrapper = new EntityEventWrapper<>(functionSpec.getQueryType(), objectMapper, entityEvent);
                    EntityFunction<E, ?> entityFunction = functionSpec.getEntityFunction();
                    result = (E) entityFunction.apply(result == null ? initialInstance : result, eventWrapper);
                } else
                    log.trace("Function Spec is not available for " + entityEvent.getEventType() + " EntityId:" + entityId + " Table:" + tableName);
            }
            if (result != null) {
                result.setId(entityId);
                result.setVersion(entityEvent.getEventKey().getVersion());
            }
        }
        return (result == null || result.getId() == null) ? null : result;
    }

    private E queryEntityInternal(String entityId, Select select, E previousEntity) throws EventStoreException {

        List<Row> entityEventDatas = cassandraSession.execute(select, PagingIterable::all);
        for (Row entityEventData : entityEventDatas) {
            EntityEvent entityEvent = convertToEntityEvent(entityEventData);
            if (entityEvent.getStatus() == EventState.CREATED || entityEvent.getStatus() == EventState.SUCCEDEED) {
                EntityFunctionSpec<E, ?> functionSpec = functionMap.get(entityEvent.getEventType());
                if (functionSpec != null) {
                    EntityEventWrapper eventWrapper = new EntityEventWrapper<>(functionSpec.getQueryType(), objectMapper, entityEvent);
                    EntityFunction<E, ?> entityFunction = functionSpec.getEntityFunction();
                    previousEntity = (E) entityFunction.apply(previousEntity, eventWrapper);
                } else
                    log.trace("Function Spec is not available for " + entityEvent.getEventType() + " EntityId:" + entityId + " Table:" + tableName);
            }
            if (previousEntity != null) {
                previousEntity.setId(entityId);
                previousEntity.setVersion(entityEvent.getEventKey().getVersion());
            }
        }
        return (previousEntity == null || previousEntity.getId() == null) ? null : previousEntity;
    }

    @Override
    public E queryEntity(EventKey eventKey) throws EventStoreException {
        return queryEntity(eventKey.getEntityId(), eventKey.getVersion());
    }

    @Override
    public List<EntityEvent> queryHistory(String entityId) throws EventStoreException {
        Select select = QueryBuilder.select().from(tableName);
        select.where(QueryBuilder.eq(CassandraEventRecorder.ENTITY_ID, entityId));
        return cassandraSession.execute(select, PagingIterable::all)
                .stream().map(CassandraViewQuery::convertToEntityEvent).collect(Collectors.toList());
    }

    @Override
    public E queryEntity(String entityId, int version) throws EventStoreException {
        Select select = QueryBuilder.select().from(tableName);
        select.where(QueryBuilder.eq(CassandraEventRecorder.ENTITY_ID, entityId));
        select.where(QueryBuilder.lte(CassandraEventRecorder.VERSION, version));
        return queryEntityInternal(entityId, select);
    }

    @Override
    public List<E> queryByOpId(String opId) throws EventStoreException {
        Select select = QueryBuilder.select(CassandraEventRecorder.ENTITY_ID).from(tableNameByOps);
        select.where(QueryBuilder.eq(CassandraEventRecorder.OP_ID, opId));
        List<Row> entityEventDatas = cassandraSession.execute(select, PagingIterable::all);

        Map<String, E> resultList = new HashMap<>();
        for (Row entityEvent : entityEventDatas) {
            String entityId = entityEvent.getString(CassandraEventRecorder.ENTITY_ID);
            if (!resultList.containsKey(entityId)) {
                E value = queryEntity(entityId);
                if (value != null)
                    resultList.put(entityId, value);
            }
        }
        return new ArrayList<>(resultList.values());
    }


    E queryEntity(String entityId, int version, E previousEntity) throws EventStoreException {
        Select select = QueryBuilder.select().from(tableName);
        select.where(QueryBuilder.eq(CassandraEventRecorder.ENTITY_ID, entityId));
        select.where(QueryBuilder.gt(CassandraEventRecorder.VERSION, previousEntity.getVersion()));
        select.where(QueryBuilder.lte(CassandraEventRecorder.VERSION, version));
        return queryEntityInternal(entityId, select, previousEntity);
    }

    @Override
    public List<E> queryByOpId(String opId, Function<String, E> findOne) throws EventStoreException {
        Select select = QueryBuilder.select(CassandraEventRecorder.ENTITY_ID, CassandraEventRecorder.VERSION).from(tableNameByOps);
        select.where(QueryBuilder.eq(CassandraEventRecorder.OP_ID, opId));
        List<Row> entityEventDatas = cassandraSession.execute(select, PagingIterable::all);
        Map<String, E> resultList = new HashMap<>();

        for (Row entityEvent : entityEventDatas) {
            String entityId = entityEvent.getString(CassandraEventRecorder.ENTITY_ID);
            int version = entityEvent.getInt(CassandraEventRecorder.VERSION);
            E snapshot = findOne.apply(entityId);
            E newEntity = null;
            if (snapshot == null || snapshot.getVersion() > version) {
                newEntity = queryEntity(entityId);
            } else if (snapshot.getVersion() < version) {
                newEntity = queryEntity(entityId, version, snapshot);
            } else {
                log.debug("Up-to-date Snapshot:" + snapshot);
            }
            if (!resultList.containsKey(entityId)) {
                if (newEntity != null)
                    resultList.put(entityId, newEntity);
            }

        }
        return new ArrayList<>(resultList.values());
    }

    @Override
    public EntityEvent queryEvent(String entityId, int version) throws EventStoreException {
        Select select = QueryBuilder.select().from(tableName);
        select.where(QueryBuilder.eq(CassandraEventRecorder.ENTITY_ID, entityId));
        select.where(QueryBuilder.eq(CassandraEventRecorder.VERSION, version));
        Row one = cassandraSession.execute(select, PagingIterable::one);
        return one == null ? null : convertToEntityEvent(one);
    }

    @Override
    public <T extends PublishedEvent> T queryEventData(String entityId, int version) throws EventStoreException {
        EntityEvent e = queryEvent(entityId, version);
        EntityFunctionSpec<E, ?> functionSpec = functionMap.get(e.getEventType());
        return new EntityEventWrapper<>((Class<T>) functionSpec.getQueryType(), objectMapper, e).getEventData();
    }

    private void addCommandSpecs(List<EntityFunctionSpec<E, ?>> commandSpec) {
        for (EntityFunctionSpec<E, ?> functionSpec : commandSpec) {
            String simpleName = functionSpec.getQueryType().getSimpleName();
            if (functionMap.containsKey(simpleName))
                throw new IllegalArgumentException("Multiple Function Spec for:" + simpleName + " 1-" + functionSpec.getClass() + " 2-" + functionMap.get(simpleName).getClass());
            functionMap.put(simpleName, functionSpec);
        }
        entityType = commandSpec.iterator().next().getEntityType();
    }


}
