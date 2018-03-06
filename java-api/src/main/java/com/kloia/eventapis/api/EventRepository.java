package com.kloia.eventapis.api;

import com.kloia.eventapis.cassandra.ConcurrencyResolver;
import com.kloia.eventapis.cassandra.ConcurrentEventException;
import com.kloia.eventapis.cassandra.EntityEvent;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.common.PublishableEvent;
import com.kloia.eventapis.common.PublishedEvent;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.view.Entity;

import java.util.List;
import java.util.function.Function;

/**
 * Created by zeldalozdemir on 21/04/2017.
 */
public interface EventRepository{

    List<EntityEvent> markFail(String opId);

    <P extends PublishableEvent> EventKey recordAndPublish(P publishedEvent) throws EventStoreException, ConcurrentEventException;

    <P extends PublishableEvent> EventKey recordAndPublish(Entity entity, P publishableEvent) throws EventStoreException, ConcurrentEventException;

    <P extends PublishableEvent> EventKey recordAndPublish(EventKey eventKey, P publishableEvent) throws EventStoreException, ConcurrentEventException;

    <P extends PublishableEvent, T extends Exception> EventKey recordAndPublish(
            Entity entity, P publishableEvent, Function<EntityEvent, ConcurrencyResolver<T>> concurrencyResolverFactory
    ) throws EventStoreException, T;

    <P extends PublishableEvent, T extends Exception> EventKey recordAndPublish(
            EventKey eventKey, P publishableEvent, Function<EntityEvent, ConcurrencyResolver<T>> concurrencyResolverFactory
    ) throws EventStoreException, T;

}
