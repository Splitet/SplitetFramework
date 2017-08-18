package com.kloia.eventapis.cassandra;

import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.exception.EventStoreException;

public interface ConcurrencyResolver<T extends Exception> {

    void tryMore() throws T;

    EventKey calculateNext(EventKey failedEventKey) throws T, EventStoreException;

}
