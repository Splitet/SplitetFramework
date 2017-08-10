package com.kloia.eventapis.cassandra;

import com.datastax.driver.core.PagingIterable;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.view.Entity;
import com.kloia.eventapis.view.EntityEventWrapper;
import com.kloia.eventapis.view.EntityFunction;
import com.kloia.eventapis.view.EntityFunctionSpec;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by zeldalozdemir on 12/02/2017.
 */
@Slf4j
public class CassandraEventRepository<E extends Entity> implements PersistentEventRepository<E> {

    private String tableName;
    private CassandraSession cassandraSession;
    //    private CassandraTemplate cassandraOperations;
    private Map<String, EntityFunctionSpec<E, ?>> functionMap = new HashMap<>();
    private ObjectMapper objectMapper;
    private Class<E> entityType;
    /*    @Getter
    private List<String> indexedFields;*/

    public CassandraEventRepository(String tableName, CassandraSession cassandraSession, ObjectMapper objectMapper) {
        this.tableName = tableName;
        this.cassandraSession = cassandraSession;
        this.objectMapper = objectMapper;
    }


    @Override
    public E queryEntity(String entityId) throws EventStoreException {
        Select select = QueryBuilder.select().from(tableName);
        select.where(QueryBuilder.eq("entityId", entityId));
        List<Row> entityEventDatas;
        entityEventDatas = cassandraSession.execute(select, PagingIterable::all);


        E result;
        try {
            result = entityType.newInstance();
        } catch (InstantiationException|IllegalAccessException e) {
            log.error(e.getMessage(),e);
            throw new EventStoreException(e);
        }
        for (Row entityEventData : entityEventDatas) {
            EntityEvent entityEvent = convertToEntityEvent(entityEventData);
            if (!entityEvent.getStatus().equals("FAILED")) {
                EntityFunctionSpec<E, ?> functionSpec = functionMap.get(entityEvent.getEventType());
                EntityEventWrapper eventWrapper = new EntityEventWrapper<>(functionSpec.getQueryType(), objectMapper, entityEvent);
                EntityFunction<E, ?> entityFunction = functionSpec.getEntityFunction();
                result = (E) entityFunction.apply(result, eventWrapper);
            }
            if (result != null) {
                result.setId(entityId);
                result.setVersion(entityEvent.getEventKey().getVersion());
            }

        }
        return result;
    }

    private EntityEvent convertToEntityEvent(Row entityEventData) throws EventStoreException {
        EventKey eventKey = new EventKey(entityEventData.getString("entityId"), entityEventData.getInt("version"));
        String opId = entityEventData.getString("opId");
        String eventData = entityEventData.getString("eventData");
//            ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(eventData);
/*            for (String indexedField : indexedFields) {
                if (entityEventData.getColumnDefinitions().contains(indexedField))
                    jsonNode.put(indexedField, entityEventData.getString(indexedField));
            }*/
        return new EntityEvent(eventKey, opId, entityEventData.getTimestamp("opDate"), entityEventData.getString("eventType"), entityEventData.getString("status"), eventData);
    }

    @Override
    public List<E> queryByOpId(String opId) throws EventStoreException {
        Select select = QueryBuilder.select("entityId").from(tableName);
        select.where(QueryBuilder.eq("opId", opId));
        List<Row> entityEventDatas = cassandraSession.execute(select, PagingIterable::all);

        Map<String, E> resultList = new HashMap<>();
        for (Row entityEvent : entityEventDatas) {
            String entityId = entityEvent.getString("entityid");
            if (!resultList.containsKey(entityId)) {
                E value = queryEntity(entityId);
                if (value != null)
                    resultList.put(entityId, value);
            }
        }
        return new ArrayList<>(resultList.values());
    }

    @Override
    public List<E> queryByField(List<Clause> clauses) throws EventStoreException {
        Select select = QueryBuilder.select("entityId").from(tableName);

        if (clauses.size() > 1)
            select.allowFiltering();
        for (Clause clause : clauses) {
            select.where(clause);
        }
        List<String> entityEvents = cassandraSession.execute(select,
                rows -> rows.all().stream().map(row -> row.getString("entityId")).collect(Collectors.toList()));

        return queryEntities(entityEvents);
    }

    @Override
    public List<E> multipleQueryByField(List<List<Clause>> multipleClauses) throws EventStoreException {
        List<String> entityEventsCollected = new ArrayList<>();
        for (List<Clause> clauses : multipleClauses) {
            Select select = QueryBuilder.select("entityId").from(tableName);

            if (clauses.size() > 1)
                select.allowFiltering();
            for (Clause clause : clauses) {
                select.where(clause);
            }
            List<String> entityEvents = cassandraSession.execute(select,
                    rows -> rows.all().stream().map(row -> row.getString("entityId")).collect(Collectors.toList()));

            entityEventsCollected.addAll(entityEvents);
        }
        return queryEntities(entityEventsCollected);
    }

    private List<E> queryEntities(List<String> entityEvents) throws EventStoreException {
        Map<String, E> resultList = new HashMap<>();
        for (String entityId : entityEvents) {
            if (!resultList.containsKey(entityId)) {
                E value = queryEntity(entityId);
                if (value != null)
                    resultList.put(entityId, value);
            }
        }
        return new ArrayList<>(resultList.values());
    }


    @Override
    public void addCommandSpecs(List<EntityFunctionSpec<E, ?>> commandSpec) {
        for (EntityFunctionSpec<E, ?> functionSpec : commandSpec) {
            functionMap.put(functionSpec.getQueryType().getSimpleName(), functionSpec);
        }
        entityType = commandSpec.iterator().next().getEntityType();
    }


    @Override
    public void recordEntityEvent(EntityEvent entityEvent) throws EventStoreException {
        Insert insert = QueryBuilder.insertInto(tableName);
        insert.value("entityid", entityEvent.getEventKey().getEntityId());
        insert.value("version", entityEvent.getEventKey().getVersion());
        insert.value("opid", entityEvent.getOpId());
        insert.value("opdate", entityEvent.getOpDate());
        insert.value("eventType", entityEvent.getEventType());
        insert.value("status", entityEvent.getStatus());
        insert.value("eventData", entityEvent.getEventData().toString());

/*        for (String indexedField : indexedFields) {
            ObjectNode eventData = (ObjectNode) entityEvent.getEventData();
            JsonNode value = eventData.findValue(indexedField);
            if (value != null && !value.isNull()) {
                insert.value(indexedField, value.asText()); // convert by type
                eventData.remove(indexedField);
            }
        }*/
        insert.ifNotExists();
        cassandraSession.execute(insert, rows -> {
            Row one = rows.one();
            if (!one.getBool(0)) {
                throw new EventStoreException("Concurrent Event from Op:" + one.getString("opid"));
            }
            return one;
        });


        Select select = QueryBuilder.select().from(tableName);
        select.where(QueryBuilder.eq("entityId", entityEvent.getEventKey().getEntityId()));
        select.where(QueryBuilder.eq("version", entityEvent.getEventKey().getVersion()));

    }

    @Override
    public void markFail(String key) {
        Select select = QueryBuilder.select().from(tableName);
        select.where(QueryBuilder.eq("opId", key));
        List<Row> entityEventDatas  = cassandraSession.execute(select, PagingIterable::all);


        entityEventDatas.forEach(entityEvent -> {
            try {
                Update update = QueryBuilder.update(tableName);
                update.where(QueryBuilder.eq("entityid", entityEvent.getString("entityid")));
                update.where(QueryBuilder.eq("version", entityEvent.getInt("version")));
                update.with(QueryBuilder.set("status", "FAILED"));
                cassandraSession.execute(update);
            } catch (RuntimeException e) {
                log.warn(e.getMessage(),e);
            }
        });

    }


}
