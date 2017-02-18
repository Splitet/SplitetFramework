package com.kloia.evented;

import com.datastax.driver.core.querybuilder.Assignment;
import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 12/02/2017.
 */
@Component
public class AggregateRepository<T extends Object> {

    private CassandraTemplate cassandraOperations;
    private ObjectMapper objectMapper;

    @Autowired
    public AggregateRepository(CassandraTemplate cassandraOperations, ObjectMapper objectMapper) {
        this.cassandraOperations = cassandraOperations;
        this.objectMapper = objectMapper;
    }


    public AggregateEvent recordAggregate(AggregateEvent aggregateEvent) {
        AggregateEvent insert = cassandraOperations.insert(aggregateEvent);
        return insert;

    }

    public T getAggregate(long entityId, Class<T> type) throws IOException {
        Select select = QueryBuilder.select().from(AggregateEvent.AGGREGATE_EVENT_TABLE);
        select.where(QueryBuilder.eq("entityId", entityId));

        List<AggregateEvent> aggregateEvents = cassandraOperations.select(select, AggregateEvent.class);
        AggregateEvent aggregateEvent = aggregateEvents.get(0);
        return  objectMapper.readerFor(type).readValue(aggregateEvent.getDescription());
    }
}
