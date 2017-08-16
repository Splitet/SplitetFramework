package com.kloia.eventapis.cassandra;

import com.datastax.driver.core.PagingIterable;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.common.EventRecorder;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.view.Entity;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by zeldalozdemir on 12/02/2017.
 */
@Slf4j
public class CassandraEventRecorder<E extends Entity> implements EventRecorder<E> {

    public static final String OP_ID = "opId";
    public static final String ENTITY_ID = "entityId";
    public static final String VERSION = "version";
    public static final String OP_DATE = "opDate";
    public static final String EVENT_TYPE = "eventType";
    public static final String STATUS = "status";
    public static final String EVENT_DATA = "eventData";
    private String tableName;
    private CassandraSession cassandraSession;
    private ObjectMapper objectMapper;

    public CassandraEventRecorder(String tableName, CassandraSession cassandraSession, ObjectMapper objectMapper) {
        this.tableName = tableName;
        this.cassandraSession = cassandraSession;
        this.objectMapper = objectMapper;
    }

/*    private EntityEvent convertToEntityEvent(Row entityEventData) throws EventStoreException {
        EventKey eventKey = new EventKey(entityEventData.getString(ENTITY_ID), entityEventData.getInt(VERSION));
        String opId = entityEventData.getString(OP_ID);
        String eventData = entityEventData.getString("eventData");
        return new EntityEvent(eventKey, opId, entityEventData.getTimestamp(OP_DATE), entityEventData.getString("eventType"), entityEventData.getString("status"), eventData);
    }*/

    //    private ConcurrencyResolver concurrencyResolver = new DefaultConcurrencyResolver();
//    private Function<E, ConcurrencyResolver> concurrencyResolverFactory;

    @Override
    public void recordEntityEvent(EntityEvent entityEvent, Function<EntityEvent, ConcurrencyResolver> concurrencyResolverFactory) throws EventStoreException, ConcurrentEventException {

        ConcurrencyResolver concurrencyResolver;
        do {
            Insert insert = createInsertQuery(entityEvent);
            log.info("Recording Event: " + insert.toString());
            ResultSet resultSet = cassandraSession.execute(insert);
            log.info("Recorded Event: " + resultSet.toString());
            if (resultSet.wasApplied()) {
                break; // success
            } else {
                concurrencyResolver = concurrencyResolverFactory.apply(entityEvent);
                if (!concurrencyResolver.hasMore()) {
                    throw new EventStoreException("Concurrent Event from Op:" + resultSet.one().getString(OP_ID));
                }
            }
            Select select = QueryBuilder.select().max(VERSION).from(tableName);
            select.where(QueryBuilder.eq(ENTITY_ID,entityEvent.getEventKey().getEntityId()));
            ResultSet execute = cassandraSession.execute(select);
            execute.wasApplied();
            int lastVersion = execute.one().getInt(0);
            entityEvent = concurrencyResolver.calculateNext(entityEvent, lastVersion);
        } while (concurrencyResolver.tryMore());




/*        Select select = QueryBuilder.select().from(tableName);
        select.where(QueryBuilder.eq(ENTITY_ID, entityEvent.getEventKey().getEntityId()));
        select.where(QueryBuilder.eq(VERSION, entityEvent.getEventKey().getVersion()));*/

    }

    private Insert createInsertQuery(EntityEvent entityEvent) {
        Insert insert = QueryBuilder.insertInto(tableName);
        insert.value(ENTITY_ID, entityEvent.getEventKey().getEntityId());
        insert.value(VERSION, entityEvent.getEventKey().getVersion());
        insert.value(OP_ID, entityEvent.getOpId());
        insert.value(OP_DATE, entityEvent.getOpDate());
        insert.value(EVENT_TYPE, entityEvent.getEventType());
        insert.value(STATUS, entityEvent.getStatus().name());
        insert.value(EVENT_DATA, entityEvent.getEventData());
        insert.ifNotExists();
        return insert;
    }


    @Override
    public List<EventKey> markFail(String key) {
        Select select = QueryBuilder.select().from(tableName);
        select.where(QueryBuilder.eq(OP_ID, key));
        List<Row> entityEventDatas  = cassandraSession.execute(select, PagingIterable::all);

        return entityEventDatas.stream().map(
                        row -> new EventKey(row.getString(ENTITY_ID), row.getInt(VERSION))
                ).filter(entityEvent -> {
                    try {
                        Update update = QueryBuilder.update(tableName);
                        update.where(QueryBuilder.eq("entityid", entityEvent.getEntityId()));
                        update.where(QueryBuilder.eq(VERSION, entityEvent.getVersion()));
                        update.with(QueryBuilder.set(STATUS, "FAILED"));
                        ResultSet execute = cassandraSession.execute(update);
                        log.info("Failure Mark Result:" + execute.toString() + " Update: " + update.toString());
                        return true;
                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                        return false;
                    }
                }).collect(Collectors.toList());

    }


}
