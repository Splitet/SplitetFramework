package com.kloia.eventapis.kafka;

import com.kloia.eventapis.pojos.Event;

/**
 * Created by zeldalozdemir on 20/04/2017.
 */
public interface IOperationRepository {

    void failOperation(String eventId, SerializableConsumer<Event> action);

    void successOperation(String eventId, SerializableConsumer<Event> action);

    void publishEvent(String topic, String event, long opDate);
}
