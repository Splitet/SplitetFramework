package com.kloia.evented;

import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.evented.domain.EntityEvent;
import org.springframework.data.cassandra.core.CassandraTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 12/02/2017.
 */
public class CassandraEventRepository<E extends Entity> implements IEventRepository<E> {

    private String tableName;
    private CassandraTemplate cassandraOperations;
    private Map<String, EntityFunctionSpec<E,?>> functionMap = new HashMap<>();
    private ObjectMapper objectMapper;


    public CassandraEventRepository(String tableName, CassandraTemplate cassandraOperations, ObjectMapper objectMapper) {
        this.tableName = tableName;
        this.cassandraOperations = cassandraOperations;
        this.objectMapper = objectMapper;
    }



    @Override
    public E queryEntity(UUID entityId) throws EventStoreException {
        Select select = QueryBuilder.select().from(tableName);
        select.where(QueryBuilder.eq("entityId", entityId));
        List<EntityEvent> entityEvents = cassandraOperations.select(select, EntityEvent.class);

        E result = null;
        for (EntityEvent entityEvent : entityEvents) {
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

    @Override
    public void addCommandSpecs(List<EntityFunctionSpec<E, ?>> commandSpec) {
        for (EntityFunctionSpec<E, ?> functionSpec : commandSpec) {
            functionMap.put(functionSpec.getQueryType().getSimpleName(), functionSpec);
        }
    }


    @Override
    public void recordEntityEvent(EntityEvent entityEvent) throws EventStoreException {
        Insert insertQuery = cassandraOperations.createInsertQuery(tableName, entityEvent, null, cassandraOperations.getConverter());
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
