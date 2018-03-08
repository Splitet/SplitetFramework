package com.kloia.sample.dto.event;

import com.kloia.eventapis.common.EventType;
import com.kloia.eventapis.common.PublishedEvent;
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
