package io.splitet.sample.dto.event;

import io.splitet.core.common.EventType;
import io.splitet.core.common.PublishedEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockAddedEvent extends PublishedEvent {
    private long addedStock;

    @Override
    public EventType getEventType() {
        return EventType.OP_SINGLE;
    }
}
