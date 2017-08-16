package com.kloia.eventapis.common;

import com.datastax.driver.core.querybuilder.Clause;
import com.kloia.eventapis.cassandra.ConcurrencyResolver;
import com.kloia.eventapis.cassandra.ConcurrentEventException;
import com.kloia.eventapis.cassandra.EntityEvent;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.view.Entity;
import com.kloia.eventapis.view.EntityFunctionSpec;

import java.util.List;
import java.util.function.Function;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
public interface EventRecorder<T extends Entity> {

    void recordEntityEvent(EntityEvent entityEvent, Function<EntityEvent, ConcurrencyResolver> concurrencyResolverFactory) throws EventStoreException, ConcurrentEventException;

    List<EventKey> markFail(String key);
}
