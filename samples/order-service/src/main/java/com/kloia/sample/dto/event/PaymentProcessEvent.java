package com.kloia.sample.dto.event;

import com.kloia.eventapis.common.EventType;
import com.kloia.eventapis.common.PublishedEvent;
import com.kloia.sample.model.PaymentInformation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zeldalozdemir on 31/01/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentProcessEvent extends PublishedEvent{
    private String orderId;
    private PaymentInformation paymentInformation;
    @Override
    public EventType getEventType() {
        return EventType.EVENT;
    }

}
