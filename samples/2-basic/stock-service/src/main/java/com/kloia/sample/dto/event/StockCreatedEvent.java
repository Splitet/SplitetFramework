package com.kloia.sample.dto.event;

import com.kloia.eventapis.common.EventType;
import com.kloia.eventapis.common.PublishableEvent;
import lombok.Data;

@Data
public class StockCreatedEvent extends PublishableEvent {
    private String stockName;
    private long remainingStock;

    @Override
    public EventType getEventType() {
        return EventType.OP_SINGLE;
    }
}
