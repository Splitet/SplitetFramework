package io.splitet.core.common;

import io.splitet.core.cassandra.ConcurrencyResolver;
import io.splitet.core.cassandra.ConcurrentEventResolver;
import io.splitet.core.cassandra.EntityEvent;
import io.splitet.core.exception.EventStoreException;
import io.splitet.core.pojos.EventState;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
public interface EventRecorder {

    <T extends Exception> EventKey recordEntityEvent(
            RecordedEvent event, long date,
            Optional<EventKey> previousEventKey,
            Function<EntityEvent, ConcurrencyResolver<T>> concurrencyResolverFactory
    )
            throws EventStoreException, T;

    <R extends RecordedEvent, T extends Exception> EventKey recordEntityEvent(
            R event, long date,
            Optional<EventKey> previousEventKey,
            Supplier<ConcurrentEventResolver<R, T>> concurrentEventResolverSupplier)
            throws EventStoreException, T;

    List<EntityEvent> markFail(String key);

    String updateEvent(EventKey eventKey, @Nullable RecordedEvent newEventData, @Nullable EventState newEventState, @Nullable String newEventType) throws EventStoreException;

    String updateEvent(EventKey eventKey, RecordedEvent newEventData) throws EventStoreException;
}
