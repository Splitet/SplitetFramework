package com.kloia.eventapis.cassandra;

import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.common.RecordedEvent;
import com.kloia.eventapis.exception.EventStoreException;
import org.apache.commons.lang3.tuple.Pair;

public interface ConcurrentEventResolver<T extends Exception> {

    void tryMore() throws T;

    Pair<EventKey, RecordedEvent> calculateNext(RecordedEvent failedEvent, EventKey failedEventKey, int lastVersion) throws T, EventStoreException;
}
