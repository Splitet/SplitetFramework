package io.splitet.sample.dto.event;

import io.splitet.core.common.EventType;
import io.splitet.core.common.PublishedEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReturnedEvent extends PublishedEvent {
    private String orderId;
    private float amount;

    @Override
    public EventType getEventType() {
        return EventType.OP_SINGLE;
    }
}
