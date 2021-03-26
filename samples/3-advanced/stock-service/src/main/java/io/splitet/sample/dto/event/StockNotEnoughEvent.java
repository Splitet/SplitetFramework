package io.splitet.sample.dto.event;

import io.splitet.core.common.EventType;
import io.splitet.core.common.PublishedEvent;
import lombok.Data;

@Data
public class StockNotEnoughEvent extends PublishedEvent {
    private String orderId;
    private long numberOfItemsSold;

    @Override
    public EventType getEventType() {
        return EventType.OP_SUCCESS;
    }
}
