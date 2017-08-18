package com.kloia.eventapis.common;

import com.datastax.driver.core.querybuilder.Clause;
import com.kloia.eventapis.cassandra.ConcurrencyResolver;
import com.kloia.eventapis.cassandra.ConcurrentEventException;
import com.kloia.eventapis.cassandra.EntityEvent;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.view.Entity;
import com.kloia.eventapis.view.EntityFunctionSpec;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
public interface EventRecorder {

    <T extends Exception> EventKey recordEntityEvent(PublishedEvent entityEvent, Optional<EventKey> previousEventKey, Function<EntityEvent, ConcurrencyResolver<T>> concurrencyResolverFactory)
            throws EventStoreException, T;

    List<EntityEvent> markFail(String key);
}
