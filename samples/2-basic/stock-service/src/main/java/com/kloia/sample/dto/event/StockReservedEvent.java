package com.kloia.sample.dto.event;

import com.kloia.eventapis.common.EventType;
import com.kloia.eventapis.common.PublishedEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockReservedEvent extends PublishedEvent {
    private String orderId;
    private long numberOfItemsSold;

    @Override
    public EventType getEventType() {
        return EventType.EVENT;
    }
}
