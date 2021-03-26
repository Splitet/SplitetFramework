package io.splitet.sample.dto.event;

import io.splitet.core.common.EventType;
import io.splitet.core.common.PublishedEvent;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderCancelledEvent extends PublishedEvent {
    @Override
    public EventType getEventType() {
        return EventType.OP_SUCCESS;
    }
}
