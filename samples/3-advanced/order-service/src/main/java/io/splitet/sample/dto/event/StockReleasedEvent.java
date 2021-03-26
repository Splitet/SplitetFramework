package io.splitet.sample.dto.event;

import io.splitet.core.common.ReceivedEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockReleasedEvent extends ReceivedEvent {
    private String orderId;
    private long numberOfItemsReleased;
}
