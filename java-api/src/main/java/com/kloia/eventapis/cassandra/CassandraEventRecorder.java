package com.kloia.eventapis.cassandra;

import com.datastax.driver.core.PagingIterable;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.api.IUserContext;
import com.kloia.eventapis.api.IdCreationStrategy;
import com.kloia.eventapis.api.Views;
import com.kloia.eventapis.api.impl.UUIDCreationStrategy;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.common.EventRecorder;
import com.kloia.eventapis.common.OperationContext;
import com.kloia.eventapis.common.PublishedEvent;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.pojos.EventState;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by zeldalozdemir on 12/02/2017.
 */
@Slf4j
public class CassandraEventRecorder implements EventRecorder {

    public static final String OP_ID = OperationContext.OP_ID;
    public static final String ENTITY_ID = "entityId";
    public static final String VERSION = "version";
    public static final String OP_DATE = "opDate";
    public static final String EVENT_TYPE = "eventType";
    public static final String STATUS = "status";
    public static final String AUDIT_INFO = "auditinfo";
    public static final String EVENT_DATA = "eventData";
    private String tableName;
    private CassandraSession cassandraSession;
    private OperationContext operationContext;
    private IUserContext userContext;
    private ObjectMapper objectMapper;
    private IdCreationStrategy idCreationStrategy = new UUIDCreationStrategy();


    public CassandraEventRecorder(String tableName, CassandraSession cassandraSession, OperationContext operationContext, IUserContext userContext, ObjectMapper objectMapper) {
        this.tableName = tableName;
        this.cassandraSession = cassandraSession;
        this.operationContext = operationContext;
        this.userContext = userContext;
        this.objectMapper = objectMapper;
    }

    public CassandraEventRecorder(String tableName, CassandraSession cassandraSession, OperationContext operationContext, IUserContext userContext, IdCreationStrategy idCreationStrategy, ObjectMapper objectMapper) {
        this(tableName, cassandraSession, operationContext, userContext, objectMapper);
        this.idCreationStrategy = idCreationStrategy;
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
    public <T extends Exception> EventKey recordEntityEvent(PublishedEvent event, long opDate, Optional<EventKey> previousEventKey, Function<EntityEvent, ConcurrencyResolver<T>> concurrencyResolverFactory)
            throws EventStoreException, T {

        ConcurrencyResolver<T> concurrencyResolver = null;

        String eventData = null;
        try {
            eventData = objectMapper.writerWithView(Views.RecordedOnly.class).writeValueAsString(event);
        } catch (IllegalArgumentException | JsonProcessingException e) {
            throw new EventStoreException(e.getMessage(), e);
        }
        EventKey eventKey;
        if (previousEventKey.isPresent()) {
            eventKey = new EventKey(previousEventKey.get().getEntityId(), previousEventKey.get().getVersion() + 1);
        } else
            eventKey = new EventKey(idCreationStrategy.nextId(), 0);


        EntityEvent entityEvent = new EntityEvent(eventKey, operationContext.getContextOpId(), new Date(opDate), event.getClass().getSimpleName(), EventState.CREATED, userContext.getAuditInfo(), eventData);

        while (true) {
            Insert insert = createInsertQuery(entityEvent);
            log.debug("Recording Event: " + insert.toString());
            ResultSet resultSet = cassandraSession.execute(insert);
            log.debug("Recorded Event: " + resultSet.toString());

            if (resultSet.wasApplied()) {
                return entityEvent.getEventKey();
            } else {
                Row one = resultSet.one();
                log.warn("!wasApplied: " + one.getBool("[applied]"));
                if (concurrencyResolver == null)
                    concurrencyResolver = concurrencyResolverFactory.apply(entityEvent);
                concurrencyResolver.tryMore();  // go on or finish

                Select select = QueryBuilder.select().max(VERSION).from(tableName);
                select.where(QueryBuilder.eq(ENTITY_ID, entityEvent.getEventKey().getEntityId()));
                ResultSet execute = cassandraSession.execute(select);
                int lastVersion = execute.one().getInt(0);
                entityEvent.setEventKey(concurrencyResolver.calculateNext(entityEvent.getEventKey(),lastVersion));
            }

        }
    }

    private Insert createInsertQuery(EntityEvent entityEvent) {
        Insert insert = QueryBuilder.insertInto(tableName);
        insert.value(ENTITY_ID, entityEvent.getEventKey().getEntityId());
        insert.value(VERSION, entityEvent.getEventKey().getVersion());
        insert.value(OP_ID, entityEvent.getOpId());
        insert.value(OP_DATE, entityEvent.getOpDate());
        insert.value(EVENT_TYPE, entityEvent.getEventType());
        insert.value(STATUS, entityEvent.getStatus().name());
        insert.value(AUDIT_INFO, entityEvent.getAuditInfo());
        insert.value(EVENT_DATA, entityEvent.getEventData());
        insert.ifNotExists();
        return insert;
    }


    @Override
    public List<EntityEvent> markFail(String key) {
        Select select = QueryBuilder.select().from(tableName);
        select.where(QueryBuilder.eq(OP_ID, key));
        List<Row> entityEventDatas = cassandraSession.execute(select, PagingIterable::all);

        return entityEventDatas.stream().map(
                CassandraViewQuery::convertToEntityEvent
        ).filter(entityEvent -> {
            try {
                Update update = QueryBuilder.update(tableName);
                update.where(QueryBuilder.eq(ENTITY_ID, entityEvent.getEventKey().getEntityId()))
                        .and(QueryBuilder.eq(VERSION, entityEvent.getEventKey().getVersion()))
                        .ifExists();
                update.with(QueryBuilder.set(STATUS, "FAILED"));
                ResultSet execute = cassandraSession.execute(update);
                log.debug("Failure Mark Result:" + execute.toString() + " Update: " + update.toString());
                return true;
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
                return false;
            }
        }).collect(Collectors.toList());

    }


}
