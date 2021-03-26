package io.splitet.core.cassandra;

import io.splitet.core.common.EventKey;
import io.splitet.core.common.RecordedEvent;
import io.splitet.core.exception.EventStoreException;
import org.apache.commons.lang3.tuple.Pair;

public interface ConcurrentEventResolver<R extends RecordedEvent, T extends Exception> {

    void tryMore() throws T;

    Pair<EventKey, ? extends RecordedEvent> calculateNext(R failedEvent, EventKey failedEventKey, int lastVersion) throws T, EventStoreException;
}
