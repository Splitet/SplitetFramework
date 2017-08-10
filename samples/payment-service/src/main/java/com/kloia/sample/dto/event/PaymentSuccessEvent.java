package com.kloia.sample.dto.event;

import com.fasterxml.jackson.annotation.JsonView;
import com.kloia.eventapis.pojos.PublishedEvent;
import com.kloia.eventapis.pojos.Views;
import lombok.Data;

@Data
public class PaymentSuccessEvent extends PublishedEvent{
    private String orderId;
    private String paymentAddress;
    private float amount;
    @JsonView(Views.RecordedOnly.class)
    private String cardInformation;
}
