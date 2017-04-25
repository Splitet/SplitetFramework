package com.kloia.evented;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zeldalozdemir on 21/04/2017.
 */
public interface EventRepository<E extends Entity>{
    void publishEvent(Event event);

    void addAggregateSpecs(List<EntityFunctionSpec<E, ?>> commandSpec);

    <D extends Serializable> EventKey recordEntityEvent(E previousEntityState, Class<? extends EntityFunctionSpec<E, D>> entitySpecClass, D eventData) throws EventStoreException;
    <D extends Serializable> EventKey recordEntityEvent(Class<? extends EntityFunctionSpec<E, D>> entitySpecClass, D eventData) throws EventStoreException;
}
