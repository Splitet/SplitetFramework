package io.splitet.core.cassandra;

import io.splitet.core.common.EventKey;
import io.splitet.core.exception.EventStoreException;

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
