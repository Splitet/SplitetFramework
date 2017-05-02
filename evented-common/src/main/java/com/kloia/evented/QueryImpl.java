package com.kloia.evented;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

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
    public T queryEntity(UUID entityId) throws EventStoreException {
        return eventRepository.queryEntity(entityId);
    }
}
