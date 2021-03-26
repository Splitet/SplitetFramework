package io.splitet.sample.dto.event;

import io.splitet.core.common.EventType;
import io.splitet.core.common.PublishedEvent;
import io.splitet.sample.model.PaymentInformation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zeldalozdemir on 31/01/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentProcessEvent extends PublishedEvent {
    private String orderId;
    private int reservedStockVersion;
    private PaymentInformation paymentInformation;

    @Override
    public EventType getEventType() {
        return EventType.EVENT;
    }

}
