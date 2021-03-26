package io.splitet.core.cassandra;

import io.splitet.core.common.EventKey;
import io.splitet.core.common.RecordedEvent;
import io.splitet.core.exception.EventStoreException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public interface ConcurrencyResolver<T extends Exception> extends ConcurrentEventResolver<RecordedEvent, T> {

    void tryMore() throws T;

    EventKey calculateNext(EventKey failedEventKey, int lastVersion) throws T, EventStoreException;

    default Pair<EventKey, RecordedEvent> calculateNext(RecordedEvent failedEvent, EventKey failedEventKey, int lastVersion) throws T, EventStoreException {
        return new ImmutablePair<>(calculateNext(failedEventKey, lastVersion), failedEvent);
    }

}
