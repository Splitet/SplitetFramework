package com.kloia.evented;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Component;

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
