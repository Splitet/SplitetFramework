package com.kloia.sample.dto.event;

import com.kloia.eventapis.common.EventType;
import com.kloia.eventapis.common.PublishedEvent;
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
