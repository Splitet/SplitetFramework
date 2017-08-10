package com.kloia.eventapis.cassandra;

import com.datastax.driver.core.querybuilder.Clause;
import com.kloia.eventapis.api.Query;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.view.Entity;

import java.util.List;

/**
 * Created by zeldalozdemir on 24/04/2017.
 */
public class QueryByPersistentEventRepositoryDelegate<T extends Entity> implements Query<T> {
    private PersistentEventRepository<T> eventRepository;

    public QueryByPersistentEventRepositoryDelegate(PersistentEventRepository<T> eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public T queryEntity(String entityId) throws EventStoreException {
        return eventRepository.queryEntity(entityId);
    }

    @Override
    public List<T> queryByOpId(String opId) throws EventStoreException {
        return eventRepository.queryByOpId(opId);
    }

    @Override
    public List<T> queryByField(List<Clause> clauses) throws EventStoreException {
        return eventRepository.queryByField(clauses);
    }

    @Override
    public List<T> multipleQueryByField(List<List<Clause>> clauses) throws EventStoreException {
        return eventRepository.multipleQueryByField(clauses);
    }
}
