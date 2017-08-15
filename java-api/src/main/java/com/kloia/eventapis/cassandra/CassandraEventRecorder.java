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

    private ConcurrencyResolver concurrencyResolver = new DefaultConcurrencyResolver();
    @Override
    public void recordEntityEvent(EntityEvent entityEvent) throws EventStoreException {
        Insert insert = QueryBuilder.insertInto(tableName);
        insert.value(ENTITY_ID, entityEvent.getEventKey().getEntityId());
        insert.value(VERSION, entityEvent.getEventKey().getVersion());
        insert.value(OP_ID, entityEvent.getOpId());
        insert.value(OP_DATE, entityEvent.getOpDate());
        insert.value(EVENT_TYPE, entityEvent.getEventType());
        insert.value(STATUS, entityEvent.getStatus().name());
        insert.value(EVENT_DATA, entityEvent.getEventData());

        insert.ifNotExists();
        log.info("Recording Event: " + insert.toString());
        do {
            ResultSet resultSet = cassandraSession.execute(insert);
            log.info("Recorded Event: " + resultSet.toString());
            Row one = resultSet.one();
            if (!one.getBool(0)) {
                throw new EventStoreException("Concurrent Event from Op:" + one.getString(OP_ID));
            }
        } while (concurrencyResolver.tryMore());




/*        Select select = QueryBuilder.select().from(tableName);
        select.where(QueryBuilder.eq(ENTITY_ID, entityEvent.getEventKey().getEntityId()));
        select.where(QueryBuilder.eq(VERSION, entityEvent.getEventKey().getVersion()));*/

    }

    @Override
    public List<EventKey> markFail(String key) {
        Select select = QueryBuilder.select().from(tableName);
        select.where(QueryBuilder.eq(OP_ID, key));
        List<Row> entityEventDatas  = cassandraSession.execute(select, PagingIterable::all);
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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
