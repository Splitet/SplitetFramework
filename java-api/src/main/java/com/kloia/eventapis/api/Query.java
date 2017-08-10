package com.kloia.eventapis.api;


import com.datastax.driver.core.querybuilder.Clause;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.view.Entity;

import java.util.List;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
public interface Query<T extends Entity> {
    T queryEntity(String entityId) throws EventStoreException;

    List<T> queryByOpId(String opId) throws EventStoreException;

    List<T> queryByField(List<Clause> clauses) throws EventStoreException;

    List<T> multipleQueryByField(List<List<Clause>> clauses) throws EventStoreException;

}
