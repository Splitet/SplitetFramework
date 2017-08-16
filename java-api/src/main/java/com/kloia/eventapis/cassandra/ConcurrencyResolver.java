package com.kloia.eventapis.cassandra;

import com.kloia.eventapis.exception.EventStoreException;

public interface ConcurrencyResolver {
    boolean tryMore();

    boolean hasMore();

    EntityEvent calculateNext(EntityEvent entityEvent, int lastVersion) throws ConcurrentEventException, EventStoreException;
}
