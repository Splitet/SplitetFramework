package com.kloia.sample.dto.event;

import com.kloia.eventapis.common.EventType;
import com.kloia.eventapis.common.PublishedEvent;
import lombok.Data;

@Data
public class StockCreatedEvent extends PublishedEvent {
    private String stockName;
    private long remainingStock;

    @Override
    public EventType getEventType() {
        return EventType.OP_SINGLE;
    }
}
