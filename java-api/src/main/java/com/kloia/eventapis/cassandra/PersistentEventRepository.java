package com.kloia.eventapis.cassandra;

import com.datastax.driver.core.querybuilder.Clause;
import com.kloia.eventapis.cassandra.EntityEvent;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.view.Entity;
import com.kloia.eventapis.view.EntityFunctionSpec;

import java.util.List;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
public interface PersistentEventRepository<T extends Entity> {
    T queryEntity(String entityId) throws EventStoreException;

    List<T> queryByOpId(String opId) throws EventStoreException;

    List<T> queryByField(List<Clause> clauses) throws EventStoreException;

    void addCommandSpecs(List<EntityFunctionSpec<T, ?>> commandSpec);

    void recordEntityEvent(EntityEvent entityEvent) throws EventStoreException;

    void markFail(String key);

    List<T> multipleQueryByField(List<List<Clause>> clauses) throws EventStoreException;
}
