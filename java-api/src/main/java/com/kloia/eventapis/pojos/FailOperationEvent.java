package com.kloia.eventapis.pojos;

import com.kloia.eventapis.api.impl.SerializableConsumer;
import lombok.Data;

import java.util.UUID;

/**
 * Created by zeldalozdemir on 24/04/2017.
 */
@Data
public class FailOperationEvent {
    private UUID eventId;


    public FailOperationEvent(UUID eventId) {

        this.eventId = eventId;
    }
}
