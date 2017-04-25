package com.kloia.eventapis.pojos;

import com.kloia.eventapis.api.impl.SerializableConsumer;

import java.util.UUID;

/**
 * Created by zeldalozdemir on 24/04/2017.
 */
public class FailOperationEvent {
    public FailOperationEvent(UUID eventId, SerializableConsumer<Event> action) {
    }
}
