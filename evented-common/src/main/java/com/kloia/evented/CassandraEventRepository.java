package com.kloia.evented;

import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zeldalozdemir on 12/02/2017.
 */
@Component
public class CassandraEventRepository<T extends Entity> implements IEventRepository<T> {

    private CassandraTemplate cassandraOperations;
    private ObjectMapper objectMapper;
    private Map<String, AggregateFunction<T>> functionMap = new HashMap<>();


    @Autowired
    public CassandraEventRepository(CassandraTemplate cassandraOperations, ObjectMapper objectMapper) {
        this.cassandraOperations = cassandraOperations;
        this.objectMapper = objectMapper;
    }



    @Override
    public T queryEntity(long entityId) throws EventStoreException {
        Select select = QueryBuilder.select().from(EntityEvent.AGGREGATE_EVENT_TABLE);
        select.where(QueryBuilder.eq("entityId", entityId));
        List<EntityEvent> entityEvents = cassandraOperations.select(select, EntityEvent.class);

        T result = null;
        for (EntityEvent entityEvent : entityEvents) {
            result = functionMap.get(entityEvent.getAggregateName()).apply(result, entityEvent);
            result.setVersion(entityEvent.getEventKey().getVersion());
        }
        return result;
    }

    @Override
    public void addAggregateSpecs(CommandSpec commandSpec) {
        functionMap.put(commandSpec.getCommandName(), commandSpec.getApply());

    }

    @Override
    public void recordAggregateEvent(EntityEvent entityEvent) throws EventStoreException {
        Insert insertQuery = cassandraOperations.createInsertQuery(EntityEvent.AGGREGATE_EVENT_TABLE, entityEvent, null, cassandraOperations.getConverter());
        insertQuery.ifNotExists();
        cassandraOperations.execute(insertQuery);
        Select select = QueryBuilder.select().from(EntityEvent.AGGREGATE_EVENT_TABLE);
        select.where(QueryBuilder.eq("entityId", entityEvent.getEventKey().getEntityId()));
        select.where(QueryBuilder.eq("version", entityEvent.getEventKey().getVersion()) );
        EntityEvent appliedEntityEvent = cassandraOperations.selectOne(select, EntityEvent.class);
        if(! appliedEntityEvent.getOpId().equals(entityEvent.getOpId()))
            throw new EventStoreException("Concurrent Event from Op:"+ appliedEntityEvent.getOpId());
    }
}
