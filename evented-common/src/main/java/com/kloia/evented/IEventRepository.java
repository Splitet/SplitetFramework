package com.kloia.evented;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
public interface IEventRepository<T extends Entity> {
    T queryEntity(long entityId) throws EventStoreException;

    void addAggregateSpecs(CommandSpec commandSpec);

    void recordAggregateEvent(EntityEvent entityEvent) throws EventStoreException;

    void markFail(UUID key);
}
