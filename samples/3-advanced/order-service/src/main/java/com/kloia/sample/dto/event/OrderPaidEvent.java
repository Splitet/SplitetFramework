package com.kloia.sample.dto.event;

import com.kloia.eventapis.common.EventType;
import com.kloia.eventapis.common.PublishedEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zeldalozdemir on 31/01/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPaidEvent extends PublishedEvent {
    private String paymentId;

    @Override
    public EventType getEventType() {
        return EventType.OP_SUCCESS;
    }

}
