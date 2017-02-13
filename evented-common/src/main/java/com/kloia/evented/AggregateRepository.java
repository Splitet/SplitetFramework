package com.kloia.evented;

import com.datastax.driver.core.querybuilder.Assignment;
import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.utils.UUIDs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Created by zeldalozdemir on 12/02/2017.
 */
@Component
public class AggregateRepository {

    private CassandraTemplate cassandraOperations;

    @Autowired
    public AggregateRepository(CassandraTemplate cassandraOperations) {
        this.cassandraOperations = cassandraOperations;
    }


    public AggregateEvent recordAggregate(AggregateEvent aggregateEvent){
        AggregateEvent insert = cassandraOperations.insert(aggregateEvent);
        return insert;

    }
}
