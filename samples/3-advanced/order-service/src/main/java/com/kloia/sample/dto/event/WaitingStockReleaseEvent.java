package com.kloia.sample.dto.event;

import com.kloia.eventapis.common.EventType;
import com.kloia.eventapis.common.PublishableEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaitingStockReleaseEvent extends PublishableEvent {
    private String stockId;
    private int reservedStockVersion;

    @Override
    public EventType getEventType() {
        return EventType.OP_START;
    }
}
