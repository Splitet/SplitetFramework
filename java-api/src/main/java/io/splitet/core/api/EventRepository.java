package io.splitet.core.api;

import io.splitet.core.cassandra.ConcurrencyResolver;
import io.splitet.core.cassandra.ConcurrentEventException;
import io.splitet.core.cassandra.ConcurrentEventResolver;
import io.splitet.core.cassandra.EntityEvent;
import io.splitet.core.common.EventKey;
import io.splitet.core.common.EventRecorder;
import io.splitet.core.common.PublishedEvent;
import io.splitet.core.exception.EventStoreException;
import io.splitet.core.view.Entity;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by zeldalozdemir on 21/04/2017.
 */
public interface EventRepository {

    List<EntityEvent> markFail(String opId);

    <P extends PublishedEvent> EventKey recordAndPublish(P publishedEvent) throws EventStoreException, ConcurrentEventException;

    <P extends PublishedEvent> EventKey recordAndPublish(Entity entity, P publishedEvent) throws EventStoreException, ConcurrentEventException;

    <P extends PublishedEvent> EventKey recordAndPublish(EventKey eventKey, P publishedEvent) throws EventStoreException, ConcurrentEventException;

    <P extends PublishedEvent, T extends Exception> EventKey recordAndPublish(
            Entity entity, P publishedEvent, Function<EntityEvent, ConcurrencyResolver<T>> concurrencyResolverFactory
    ) throws EventStoreException, T;

    <P extends PublishedEvent, T extends Exception> EventKey recordAndPublish(
            EventKey eventKey, P publishedEvent, Function<EntityEvent, ConcurrencyResolver<T>> concurrencyResolverFactory
    ) throws EventStoreException, T;

    <P extends PublishedEvent, T extends Exception> EventKey recordAndPublish(
            Entity entity, P publishedEvent, Supplier<ConcurrentEventResolver<P, T>> concurrencyResolverFactory
    ) throws EventStoreException, T;

    <P extends PublishedEvent, T extends Exception> EventKey recordAndPublish(
            EventKey eventKey, P publishedEvent, Supplier<ConcurrentEventResolver<P, T>> concurrencyResolverFactory
    ) throws EventStoreException, T;

    EventRecorder getEventRecorder();

}
