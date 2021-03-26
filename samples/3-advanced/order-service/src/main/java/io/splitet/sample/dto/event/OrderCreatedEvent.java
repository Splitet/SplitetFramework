package io.splitet.sample.dto.event;

import io.splitet.core.common.EventType;
import io.splitet.core.common.PublishedEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent extends PublishedEvent {
    private String stockId;
    private int orderAmount;
    private String description;

    @Override
    public EventType getEventType() {
        return EventType.OP_SINGLE;
    }
}
