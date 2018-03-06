package com.kloia.eventapis.api;


import com.kloia.eventapis.cassandra.EntityEvent;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.common.PublishableEvent;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.view.Entity;

import java.util.List;
import java.util.function.Function;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
public interface ViewQuery<E extends Entity> {
    E queryEntity(String entityId) throws EventStoreException;

    List<EntityEvent> queryHistory(String entityId) throws EventStoreException;

    E queryEntity(String entityId, int version) throws EventStoreException;

    E queryEntity(EventKey eventKey) throws EventStoreException;

    List<E> queryByOpId(String opId) throws EventStoreException;

    List<E> queryByOpId(String key, Function<String, E> findOne) throws EventStoreException;

    EntityEvent queryEvent(String entityId, int version) throws EventStoreException;

    <T extends PublishableEvent> T queryEventData(String entityId, int version) throws EventStoreException;

/*    List<T> queryByField(List<Clause> clauses) throws EventStoreException;

    List<T> multipleQueryByField(List<List<Clause>> clauses) throws EventStoreException;*/

}
