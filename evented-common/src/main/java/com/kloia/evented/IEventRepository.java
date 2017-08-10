package com.kloia.evented;

import com.datastax.driver.core.querybuilder.Clause;
import com.kloia.evented.domain.EntityEvent;

import java.util.List;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
public interface IEventRepository<T extends Entity> {
    T queryEntity(String entityId) throws EventStoreException;

    List<T> queryByOpId(String opId) throws EventStoreException;

    List<T> queryByField(List<Clause> clauses) throws EventStoreException;

    void addCommandSpecs(List<EntityFunctionSpec<T, ?>> commandSpec);

    void recordEntityEvent(EntityEvent entityEvent) throws EventStoreException;

    void markFail(String key);

    List<T> multipleQueryByField(List<List<Clause>> clauses) throws EventStoreException;
}
