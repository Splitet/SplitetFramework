package com.kloia.evented;

/**
 * Created by zeldalozdemir on 21/02/2017.
 */
@FunctionalInterface
public interface AggregateFunction<R extends Object> {
    R apply(R previous, EntityEvent t) throws EventStoreException;
}