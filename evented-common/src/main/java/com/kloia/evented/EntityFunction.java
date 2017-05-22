package com.kloia.evented;

/**
 * Created by zeldalozdemir on 21/02/2017.
 */
@FunctionalInterface
public interface EntityFunction<E, W> {
    E apply(E previous, EntityEventWrapper<W> event) throws EventStoreException;
}