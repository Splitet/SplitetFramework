package com.kloia.eventapis.view;


import java.util.List;

public interface SnapshotRepository<T> {
    <S extends T> List<S> save(Iterable<S> entities);
}
