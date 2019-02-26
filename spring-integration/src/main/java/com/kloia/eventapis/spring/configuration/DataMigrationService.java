package com.kloia.eventapis.spring.configuration;


import com.kloia.eventapis.api.ViewQuery;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.common.EventRecorder;
import com.kloia.eventapis.common.RecordedEvent;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.pojos.EventState;
import com.kloia.eventapis.view.Entity;
import com.kloia.eventapis.view.SnapshotRepository;

import javax.annotation.Nullable;

public class DataMigrationService<T extends Entity> {

    private EventRecorder eventRecorder;
    private ViewQuery<T> viewQuery;
    private SnapshotRepository<T, String> snapshotRepository;

    public DataMigrationService(EventRecorder eventRecorder, ViewQuery<T> viewQuery, SnapshotRepository<T, String> snapshotRepository) {
        this.eventRecorder = eventRecorder;
        this.viewQuery = viewQuery;
        this.snapshotRepository = snapshotRepository;
    }

    public T updateEvent(EventKey eventKey, boolean snapshot, @Nullable RecordedEvent newEventData, @Nullable EventState newEventState, @Nullable String newEventType) throws EventStoreException {
        eventRecorder.updateEvent(eventKey, newEventData, newEventState, newEventType);
        T entity = viewQuery.queryEntity(eventKey.getEntityId());
        if (snapshot)
            entity = snapshotRepository.save(entity);
        return entity;
    }

    public T updateEvent(EventKey eventKey, boolean snapshot, RecordedEvent newEventData) throws EventStoreException {
        return this.updateEvent(eventKey, snapshot, newEventData, null, null);
    }

    public T snapshotOnly(String entityId) throws EventStoreException {
        T entity = viewQuery.queryEntity(entityId);
        return snapshotRepository.save(entity);
    }
}
