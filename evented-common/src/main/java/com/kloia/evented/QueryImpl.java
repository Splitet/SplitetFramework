package com.kloia.evented;

import com.datastax.driver.core.querybuilder.Clause;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 24/04/2017.
 */
public class QueryImpl<T extends Entity> implements Query<T> {
    private IEventRepository<T> eventRepository;

    @Autowired
    public QueryImpl(IEventRepository<T> eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public T queryEntity(String entityId) throws EventStoreException {
        return eventRepository.queryEntity(entityId);
    }

    @Override
    public List<T> queryByOpId(UUID opId) throws EventStoreException {
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
