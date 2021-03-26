package io.splitet.core.cassandra;

import com.datastax.driver.core.PagingIterable;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.splitet.core.api.IUserContext;
import io.splitet.core.api.IdCreationStrategy;
import io.splitet.core.api.Views;
import io.splitet.core.api.impl.UUIDCreationStrategy;
import io.splitet.core.common.EventKey;
import io.splitet.core.common.EventRecorder;
import io.splitet.core.common.OperationContext;
import io.splitet.core.common.RecordedEvent;
import io.splitet.core.exception.EventStoreException;
import io.splitet.core.pojos.EventState;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
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
    private String tableNameByOps;
    private CassandraSession cassandraSession;
    private OperationContext operationContext;
    private IUserContext userContext;
    private ObjectMapper objectMapper;
    private IdCreationStrategy idCreationStrategy = new UUIDCreationStrategy();


    public CassandraEventRecorder(String tableName,
                                  CassandraSession cassandraSession,
                                  OperationContext operationContext,
                                  IUserContext userContext,
                                  ObjectMapper objectMapper) {
        this.tableName = tableName;
        this.tableNameByOps = tableName + "_byOps";
        this.cassandraSession = cassandraSession;
        this.operationContext = operationContext;
        this.userContext = userContext;
        this.objectMapper = objectMapper;
    }

    public CassandraEventRecorder(String tableName,
                                  CassandraSession cassandraSession,
                                  OperationContext operationContext,
                                  IUserContext userContext,
                                  IdCreationStrategy idCreationStrategy,
                                  ObjectMapper objectMapper) {
        this(tableName, cassandraSession, operationContext, userContext, objectMapper);
        this.idCreationStrategy = idCreationStrategy;
    }

    public String getTableName() {
        return tableName;
    }

    private <R extends RecordedEvent, T extends Exception> EventKey recordEntityEventInternal(
            R event,
            long opDate,
            Optional<EventKey> previousEventKey,
            Function<EntityEvent, ConcurrentEventResolver<R, T>> concurrencyResolverFactory) throws EventStoreException, T {


        ConcurrentEventResolver<R, T> concurrencyResolver = null;

        String eventData = createEventStr(event);

        EventKey eventKey;
        if (previousEventKey.isPresent()) {
            eventKey = new EventKey(previousEventKey.get().getEntityId(), previousEventKey.get().getVersion() + 1);
        } else
            eventKey = new EventKey(idCreationStrategy.nextId(), 0);


        EntityEvent entityEvent = new EntityEvent(eventKey,
                operationContext.getContextOpId(), new Date(opDate), event.getEventName(),
                EventState.CREATED, userContext.getAuditInfo(), eventData);

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
                Pair<EventKey, ? extends RecordedEvent> newData = concurrencyResolver.calculateNext(event, entityEvent.getEventKey(), lastVersion);
                entityEvent.setEventKey(newData.getKey());
                entityEvent.setEventData(createEventStr(newData.getValue()));
            }

        }
    }

    @Override
    public <R extends RecordedEvent, T extends Exception> EventKey recordEntityEvent(
            R event,
            long opDate,
            Optional<EventKey> previousEventKey,
            Supplier<ConcurrentEventResolver<R, T>> concurrentEventResolverSupplier) throws EventStoreException, T {
        return recordEntityEventInternal(event, opDate, previousEventKey, entityEvent -> concurrentEventResolverSupplier.get());
    }

    @Override
    public <T extends Exception> EventKey recordEntityEvent(
            RecordedEvent event, long opDate, Optional<EventKey> previousEventKey, Function<EntityEvent, ConcurrencyResolver<T>> concurrencyResolverFactory
    ) throws EventStoreException, T {
        return recordEntityEventInternal(event, opDate, previousEventKey, concurrencyResolverFactory::apply);
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
        Select select = QueryBuilder.select().from(tableNameByOps);
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


    @Override
    public String updateEvent(EventKey eventKey, @Nullable RecordedEvent newEventData, @Nullable EventState newEventState, @Nullable String newEventType) throws EventStoreException {
        Update update = QueryBuilder.update(tableName);
        update.where(QueryBuilder.eq(ENTITY_ID, eventKey.getEntityId()))
                .and(QueryBuilder.eq(VERSION, eventKey.getVersion()))
                .ifExists();
        if (newEventData != null)
            update.with(QueryBuilder.set(EVENT_DATA, createEventStr(newEventData)));
        if (newEventState != null)
            update.with(QueryBuilder.set(STATUS, newEventState.name()));
        if (newEventType != null)
            update.with(QueryBuilder.set(EVENT_TYPE, newEventType));
        try {
            ResultSet execute = cassandraSession.execute(update);
            log.debug("Update Event, Result:" + execute.toString() + " Update: " + update.toString());
            return execute.toString();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            throw new EventStoreException(e.getMessage(), e);
        }
    }

    @Override
    public String updateEvent(EventKey eventKey, RecordedEvent newEventData) throws EventStoreException {
        assert newEventData != null;
        return updateEvent(eventKey, newEventData, null, null);
    }

    private String createEventStr(RecordedEvent newEventData) throws EventStoreException {
        try {
            return objectMapper.writerWithView(Views.RecordedOnly.class).writeValueAsString(newEventData);
        } catch (IllegalArgumentException | JsonProcessingException e) {
            throw new EventStoreException(e.getMessage(), e);
        }
    }


}
