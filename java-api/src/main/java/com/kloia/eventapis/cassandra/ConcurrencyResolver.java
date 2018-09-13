package com.kloia.eventapis.cassandra;

import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.common.RecordedEvent;
import com.kloia.eventapis.exception.EventStoreException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public interface ConcurrencyResolver<T extends Exception> extends ConcurrentEventResolver<T> {

    void tryMore() throws T;

    EventKey calculateNext(EventKey failedEventKey, int lastVersion) throws T, EventStoreException;

    default Pair<EventKey, RecordedEvent> calculateNext(RecordedEvent failedEvent, EventKey failedEventKey, int lastVersion) throws T, EventStoreException {
        return new ImmutablePair<>(calculateNext(failedEventKey, lastVersion), failedEvent);
    }

}
