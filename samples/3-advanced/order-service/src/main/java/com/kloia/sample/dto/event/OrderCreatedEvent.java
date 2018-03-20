package com.kloia.sample.dto.event;

import com.kloia.eventapis.common.EventType;
import com.kloia.eventapis.common.PublishedEvent;
import com.kloia.eventapis.common.ReceivedEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent extends ReceivedEvent {
    private String stockId;
    private int orderAmount;
    private String description;

//    @Override
//    public EventType getEventType() {
//        return EventType.OP_SINGLE;
//    }
}
