package io.splitet.core.common;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zeldalozdemir on 21/04/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class ReceivedEvent implements RecordedEvent {

    private EventKey sender;

    private EventType eventType;

    public EventType getEventType() {
        return eventType;
    }
}
