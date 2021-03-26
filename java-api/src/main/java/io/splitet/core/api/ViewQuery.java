package io.splitet.core.api;


import io.splitet.core.cassandra.EntityEvent;
import io.splitet.core.common.EventKey;
import io.splitet.core.common.PublishedEvent;
import io.splitet.core.exception.EventStoreException;
import io.splitet.core.view.Entity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
public interface ViewQuery<E extends Entity> {
    E queryEntity(String entityId) throws EventStoreException;

    E queryEntity(String entityId, int version) throws EventStoreException;

    E queryEntity(EventKey eventKey) throws EventStoreException;

    E queryEntity(String entityId, @Nullable Integer version, E previousEntity) throws EventStoreException;

    List<E> queryByOpId(String opId) throws EventStoreException;

    List<E> queryByOpId(String key, Function<String, E> findOne) throws EventStoreException;

    List<EntityEvent> queryHistory(String entityId) throws EventStoreException;

    EntityEvent queryEvent(String entityId, int version) throws EventStoreException;

    <T extends PublishedEvent> T queryEventData(String entityId, int version) throws EventStoreException;

    List<EventKey> queryEventKeysByOpId(String opId);

/*    List<T> queryByField(List<Clause> clauses) throws EventStoreException;

    List<T> multipleQueryByField(List<List<Clause>> clauses) throws EventStoreException;*/

}
