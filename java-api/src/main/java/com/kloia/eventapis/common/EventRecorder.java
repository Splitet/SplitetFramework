package com.kloia.eventapis.common;

import com.kloia.eventapis.cassandra.ConcurrencyResolver;
import com.kloia.eventapis.cassandra.EntityEvent;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.pojos.EventState;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
public interface EventRecorder {

    <T extends Exception> EventKey recordEntityEvent(
            RecordedEvent entityEvent, long date,
            Optional<EventKey> previousEventKey, Function<EntityEvent,
            ConcurrencyResolver<T>> concurrencyResolverFactory)
            throws EventStoreException, T;

    List<EntityEvent> markFail(String key);

    String updateEvent(EventKey eventKey, @Nullable RecordedEvent newEventData, @Nullable EventState newEventState, @Nullable String newEventType) throws EventStoreException;

    String updateEvent(EventKey eventKey, RecordedEvent newEventData) throws EventStoreException;
}
