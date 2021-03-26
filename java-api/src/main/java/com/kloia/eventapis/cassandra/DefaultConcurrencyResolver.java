package com.kloia.eventapis.cassandra;

import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.exception.EventStoreException;

public class DefaultConcurrencyResolver implements ConcurrencyResolver<ConcurrentEventException> {
    @Override
    public void tryMore() throws ConcurrentEventException {
        throw new ConcurrentEventException("Concurrent Events");
    }

    @Override
    public EventKey calculateNext(EventKey eventKey, int lastVersion) throws EventStoreException, ConcurrentEventException {
        throw new ConcurrentEventException("Concurrent Events for:" + eventKey);
    }
}
