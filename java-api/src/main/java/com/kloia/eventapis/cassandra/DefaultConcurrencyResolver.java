package com.kloia.eventapis.cassandra;

import com.kloia.eventapis.exception.EventStoreException;

public class DefaultConcurrencyResolver implements ConcurrencyResolver {
    @Override
    public boolean tryMore() {
        return false;
    }

    @Override
    public boolean hasMore() {
        return false;
    }

    @Override
    public EntityEvent calculateNext(EntityEvent entityEvent, int lastVersion) throws ConcurrentEventException, EventStoreException {
        throw new ConcurrentEventException();
    }
}
