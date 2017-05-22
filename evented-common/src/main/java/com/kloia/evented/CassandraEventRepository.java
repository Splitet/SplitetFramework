package com.kloia.evented;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kloia.evented.domain.EntityEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.cassandra.core.CassandraTemplate;

import java.io.IOException;
import java.util.*;

/**
 * Created by zeldalozdemir on 12/02/2017.
 */
@Slf4j
public class CassandraEventRepository<E extends Entity> implements IEventRepository<E> {

    private String tableName;
    private CassandraTemplate cassandraOperations;
    private Map<String, EntityFunctionSpec<E,?>> functionMap = new HashMap<>();
    private ObjectMapper objectMapper;
    @Getter
    private List<String> indexedFields;

    public CassandraEventRepository(String tableName, CassandraTemplate cassandraOperations, ObjectMapper objectMapper) {
        this.tableName = tableName;
        this.cassandraOperations = cassandraOperations;
        this.objectMapper = objectMapper;
    }

    public CassandraEventRepository(String tableName, CassandraTemplate cassandraOperations, ObjectMapper objectMapper, List<String> indexedFields) {
        this.tableName = tableName;
        this.cassandraOperations = cassandraOperations;
        this.objectMapper = objectMapper;
        this.indexedFields = indexedFields;
    }



    @Override
    public E queryEntity(UUID entityId) throws EventStoreException {
        Select select = QueryBuilder.select().from(tableName);
        select.where(QueryBuilder.eq("entityId", entityId));
//        List<EntityEvent> entityEvents = cassandraOperations.select(select, EntityEvent.class);
        List<Row> entityEventDatas = cassandraOperations.select(select, Row.class);


        E result = null;
        for (Row entityEventData : entityEventDatas) {
            EntityEvent entityEvent = convertToEntityEvent(entityEventData);
            if(!entityEvent.getStatus().equals("FAILED")){
                EntityFunctionSpec<E, ?> functionSpec = functionMap.get(entityEvent.getEventType());
                EntityEventWrapper eventWrapper = new EntityEventWrapper<>(functionSpec.getQueryType(),objectMapper,entityEvent);
                EntityFunction<E, ?> entityFunction = functionSpec.getEntityFunction();
                result = (E) entityFunction.apply(result, eventWrapper);
            }
            if(result != null){
                result.setId(entityId);
                result.setVersion(entityEvent.getEventKey().getVersion());
            }

        }
        return result;
    }

    private EntityEvent convertToEntityEvent(Row entityEventData) throws EventStoreException {
        try {
            EventKey eventKey = new EventKey(entityEventData.getUUID("entityId"),entityEventData.getLong("version"));
            UUID opId = entityEventData.getUUID("opId");
            String eventData = entityEventData.getString("eventData");
            ObjectNode jsonNode = (ObjectNode)objectMapper.readTree(eventData);
            for (String indexedField : indexedFields) {
                if(entityEventData.getColumnDefinitions().contains(indexedField))
                    jsonNode.put(indexedField,entityEventData.getString(indexedField));
            }
            return new EntityEvent(eventKey, opId,entityEventData.getTimestamp("opDate"), entityEventData.getString("eventType"),entityEventData.getString("status"), jsonNode);
        } catch (IOException e) {
            log.error(e.getMessage(),e);
            throw new EventStoreException("Error "+e.getMessage()+ " while Reading Event For:"+entityEventData,e);
        }
    }

    @Override
    public List<E> queryByOpId(UUID opId) throws EventStoreException {
        Select select = QueryBuilder.select("entityId").from(tableName);
        select.where(QueryBuilder.eq("opId", opId));
        List<EntityEvent> entityEvents = cassandraOperations.select(select, EntityEvent.class);

        Map<UUID,E> resultList = new HashMap<>();
        for (EntityEvent entityEvent : entityEvents) {
            UUID entityId = entityEvent.getEventKey().getEntityId();
            if(!resultList.containsKey(entityId)){
                resultList.put(entityId,queryEntity(entityId));
            }
        }
        return new ArrayList<>(resultList.values());
    }

    @Override
    public List<E> queryByField(List<Clause> clauses) throws EventStoreException {
        Select select = QueryBuilder.select("entityId").from(tableName);
        if(clauses.size() > 1)
            select.allowFiltering();
        for (Clause clause : clauses) {
            select.where(clause);
        }
        List<UUID> entityEvents = cassandraOperations.select(select, UUID.class);


        Map<UUID,E> resultList = new HashMap<>();
        for (UUID entityId : entityEvents) {
            if(!resultList.containsKey(entityId)){
                resultList.put(entityId,queryEntity(entityId));
            }
        }
        return new ArrayList<>(resultList.values());
    }



    @Override
    public void addCommandSpecs(List<EntityFunctionSpec<E, ?>> commandSpec) {
        for (EntityFunctionSpec<E, ?> functionSpec : commandSpec) {
            functionMap.put(functionSpec.getClass().getSimpleName(), functionSpec);
        }
    }


    @Override
    public void recordEntityEvent(EntityEvent entityEvent) throws EventStoreException {
        Insert insertQuery = cassandraOperations.createInsertQuery(tableName, entityEvent, null, cassandraOperations.getConverter());
        for (String indexedField : indexedFields) {
            ObjectNode eventData = (ObjectNode) entityEvent.getEventData();
            JsonNode value =  eventData.findValue(indexedField);
            if(value != null){
                insertQuery.value(indexedField,value.textValue()); // convert by type
                eventData.remove(indexedField);
            }
        }
        insertQuery.ifNotExists();
        cassandraOperations.execute(insertQuery);
        Select select = QueryBuilder.select().from(tableName);
        select.where(QueryBuilder.eq("entityId", entityEvent.getEventKey().getEntityId()));
        select.where(QueryBuilder.eq("version", entityEvent.getEventKey().getVersion()) );
        EntityEvent appliedEntityEvent = cassandraOperations.selectOne(select, EntityEvent.class);
        if(! appliedEntityEvent.getOpId().equals(entityEvent.getOpId()))
            throw new EventStoreException("Concurrent Event from Op:"+ appliedEntityEvent.getOpId());
    }

    @Override
    public void markFail(UUID key) {
        Select select = QueryBuilder.select().from(tableName);
        select.where(QueryBuilder.eq("opId", key));
        List<EntityEvent> entityEvents = cassandraOperations.select(select, EntityEvent.class);

        entityEvents.forEach(entityEvent -> {
            entityEvent.setStatus("FAILED");
            Update updateQuery = cassandraOperations.createUpdateQuery(tableName, entityEvent, null, cassandraOperations.getConverter());
            cassandraOperations.execute(updateQuery);
        });

    }
}
