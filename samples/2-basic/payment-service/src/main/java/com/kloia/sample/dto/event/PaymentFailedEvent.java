package com.kloia.sample.dto.event;

import com.fasterxml.jackson.annotation.JsonView;
import com.kloia.eventapis.api.Views;
import com.kloia.eventapis.common.EventType;
import com.kloia.eventapis.common.PublishableEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentFailedEvent extends PublishableEvent {
    private String orderId;
    private String paymentAddress;
    private float amount;
    @JsonView(Views.RecordedOnly.class)
    private String cardInformation;

    @Override
    public EventType getEventType() {
        return EventType.OP_FAIL;
    }
}
