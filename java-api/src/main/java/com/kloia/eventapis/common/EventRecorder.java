package com.kloia.eventapis.common;

import com.datastax.driver.core.querybuilder.Clause;
import com.kloia.eventapis.cassandra.EntityEvent;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.view.Entity;
import com.kloia.eventapis.view.EntityFunctionSpec;

import java.util.List;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
public interface EventRecorder<T extends Entity> {

    void recordEntityEvent(EntityEvent entityEvent) throws EventStoreException;

    List<EventKey> markFail(String key);
}
