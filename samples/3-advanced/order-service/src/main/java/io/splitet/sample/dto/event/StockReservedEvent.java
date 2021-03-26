package io.splitet.sample.dto.event;

import io.splitet.core.common.ReceivedEvent;
import lombok.Data;

@Data
public class StockReservedEvent extends ReceivedEvent {
    private String stockId;
    private String orderId;
    private long numberOfItemsSold;
}
