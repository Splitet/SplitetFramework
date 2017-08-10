package com.kloia.evented;


import com.datastax.driver.core.querybuilder.Clause;

import java.util.List;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
public interface Query<T extends Entity> {
    T queryEntity(String entityId) throws EventStoreException;

    List<T> queryByOpId(String opId) throws EventStoreException;

    List<T> queryByField(List<Clause> clauses) throws EventStoreException;

    List<T> multipleQueryByField(List<List<Clause>> clauses) throws EventStoreException;

}
