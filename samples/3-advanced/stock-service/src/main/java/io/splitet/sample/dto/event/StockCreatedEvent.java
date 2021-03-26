package io.splitet.sample.dto.event;

import io.splitet.core.common.EventType;
import io.splitet.core.common.PublishedEvent;
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
