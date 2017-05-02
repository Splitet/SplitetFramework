package com.kloia.evented;


import java.util.UUID;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
public interface Query<T extends Entity> {
    T queryEntity(UUID entityId) throws EventStoreException;

}
