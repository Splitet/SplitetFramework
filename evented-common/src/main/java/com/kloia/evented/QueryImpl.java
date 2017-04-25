package com.kloia.evented;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by zeldalozdemir on 24/04/2017.
 */
@Service
public class QueryImpl<T extends Entity> implements Query<T> {
    private CassandraEventRepository<T> cassandraEventRepository;

    @Autowired
    public QueryImpl(CassandraEventRepository<T> cassandraEventRepository) {
        this.cassandraEventRepository = cassandraEventRepository;
    }

    @Override
    public T queryEntity(UUID entityId) throws EventStoreException {
        return cassandraEventRepository.queryEntity(entityId);
    }
}
