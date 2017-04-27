package com.kloia.evented;

import com.kloia.eventapis.pojos.PublishedEvent;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 21/04/2017.
 */
public interface EventRepository<E extends Entity>{
    <P extends PublishedEvent> void publishEvent(P publishedEvent) throws IOException;

    void addCommandSpecs(List<EntityFunctionSpec<E, ?>> commandSpec);

    <D extends Serializable> EventKey recordEntityEvent(E previousEntityState, Class<? extends EntityFunctionSpec<E, D>> entitySpecClass, D eventData) throws EventStoreException;
    <D extends Serializable> EventKey recordEntityEvent(Class<? extends EntityFunctionSpec<E, D>> entitySpecClass, D eventData) throws EventStoreException;

    void markFail(UUID opId);
}
