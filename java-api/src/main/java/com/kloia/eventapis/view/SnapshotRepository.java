package com.kloia.eventapis.view;


import java.util.List;
import java.util.Optional;

@SuppressWarnings("checkstyle:InterfaceTypeParameterName")
public interface SnapshotRepository<T, ID> {
    <S extends T> List<S> saveAll(Iterable<S> entities);

    <S extends T> S save(S entity);

    void flush();

    Optional<T> findById(ID id);
}
