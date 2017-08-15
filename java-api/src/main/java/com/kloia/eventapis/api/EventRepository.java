package com.kloia.eventapis.api;

import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.common.PublishedEvent;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.view.Entity;

import java.util.List;

/**
 * Created by zeldalozdemir on 21/04/2017.
 */
public interface EventRepository{
//    <P extends PublishedOnly> void publishEvent(P publishedEvent) throws EventPulisherException;

//    <D> EventKey recordEntityEvent(E previousEntityState, D eventData) throws EventStoreException;
//    <D> EventKey recordEntityEvent(D eventData) throws EventStoreException;

    List<EventKey> markFail(String opId);

    <P extends PublishedEvent> EventKey recordAndPublish(P publishedEvent) throws EventStoreException;
    <P extends PublishedEvent> EventKey recordAndPublish(Entity entity, P publishedEvent) throws EventStoreException;
    <P extends PublishedEvent> EventKey recordAndPublish(EventKey eventKey, P publishedEvent) throws EventStoreException;

}
