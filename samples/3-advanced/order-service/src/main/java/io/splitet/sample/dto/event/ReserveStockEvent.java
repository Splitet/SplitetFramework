package io.splitet.sample.dto.event;

import com.fasterxml.jackson.annotation.JsonView;
import io.splitet.core.api.Views;
import io.splitet.core.common.EventType;
import io.splitet.core.common.PublishedEvent;
import io.splitet.sample.model.PaymentInformation;
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
