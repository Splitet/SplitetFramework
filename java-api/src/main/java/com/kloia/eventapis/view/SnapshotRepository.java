package com.kloia.eventapis.view;


import java.io.Serializable;
import java.util.List;

@SuppressWarnings("checkstyle:InterfaceTypeParameterName")
public interface SnapshotRepository<T, ID extends Serializable> {
    <S extends T> List<S> save(Iterable<S> entities);

    <S extends T> S save(S entity);

    void flush();

    T findOne(ID id);
}
