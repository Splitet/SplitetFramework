package com.kloia.eventapis.kafka;

import com.kloia.eventapis.common.Context;
import com.kloia.eventapis.pojos.Event;

import java.util.UUID;

/**
 * Created by zeldalozdemir on 20/04/2017.
 */
public interface IOperationRepository {

    void publishEvent(String name, PublishedEventWrapper event);

    void failOperation(Context context, String eventId, SerializableConsumer<Event> action);

    void successOperation(Context context, String eventId, SerializableConsumer<Event> action);
}
