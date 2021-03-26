package io.splitet.sample.dto.event;

import com.fasterxml.jackson.annotation.JsonView;
import io.splitet.core.api.Views;
import io.splitet.core.common.EventType;
import io.splitet.core.common.PublishedEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentFailedEvent extends PublishedEvent {
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
