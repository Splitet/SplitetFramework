package com.kloia.eventapis.view;


import java.io.Serializable;
import java.util.List;

public interface SnapshotRepository<T, ID extends Serializable> {
    <S extends T> List<S> save(Iterable<S> entities);
    T findOne(ID id);
}
