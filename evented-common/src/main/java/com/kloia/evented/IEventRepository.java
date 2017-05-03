package com.kloia.evented;

import com.kloia.evented.domain.EntityEvent;

import java.util.List;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
public interface IEventRepository<T extends Entity> {
    T queryEntity(UUID entityId) throws EventStoreException;

    void addCommandSpecs(List<EntityFunctionSpec<T, ?>> commandSpec);

    void recordEntityEvent(EntityEvent entityEvent) throws EventStoreException;

    void markFail(UUID key);
}
