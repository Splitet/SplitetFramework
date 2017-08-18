package com.kloia.sample.dto.event;

import com.fasterxml.jackson.annotation.JsonView;
import com.kloia.eventapis.common.EventType;
import com.kloia.eventapis.common.PublishedEvent;
import com.kloia.eventapis.api.Views;
import com.kloia.sample.model.PaymentInformation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReserveStockEvent extends PublishedEvent {
    private String stockId;
    private long numberOfItemsSold;
    @JsonView(Views.RecordedOnly.class)
    private PaymentInformation paymentInformation;
    @Override
    public EventType getEventType() {
        return EventType.OP_START;
    }
}
