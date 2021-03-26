package io.splitet.sample.dto.event;

import com.fasterxml.jackson.annotation.JsonView;
import io.splitet.core.api.Views;
import io.splitet.core.common.EventType;
import io.splitet.core.common.PublishedEvent;
import lombok.Data;

@Data
public class PaymentSuccessEvent extends PublishedEvent {
    private String orderId;
    private String paymentAddress;
    private float amount;
    @JsonView(Views.RecordedOnly.class)
    private String cardInformation;

    @Override
    public EventType getEventType() {
        return EventType.EVENT;
    }
}
