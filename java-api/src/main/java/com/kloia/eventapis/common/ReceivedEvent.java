package com.kloia.eventapis.common;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zeldalozdemir on 21/04/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class ReceivedEvent extends PublishedEvent {

    private EventType eventType;

    public final EventType getEventType() {
        return eventType;
    }
}
