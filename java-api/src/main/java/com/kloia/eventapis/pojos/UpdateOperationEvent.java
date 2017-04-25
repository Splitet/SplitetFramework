package com.kloia.eventapis.pojos;

import com.kloia.eventapis.api.impl.SerializableConsumer;
import lombok.Data;

import java.util.UUID;

/**
 * Created by zeldalozdemir on 20/04/2017.
 */
@Data
public class UpdateOperationEvent implements IOperationEvents {
    private Event event;
    private UUID eventId;
    private SerializableConsumer<Event> action;

    public UpdateOperationEvent() {
    }

    public UpdateOperationEvent(UUID eventId, SerializableConsumer<Event> action) {
        this.eventId = eventId;
        this.action = action;
    }

}
