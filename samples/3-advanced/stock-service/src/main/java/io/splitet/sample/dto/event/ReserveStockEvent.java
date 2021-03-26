package io.splitet.sample.dto.event;

import io.splitet.core.common.ReceivedEvent;
import lombok.Data;

@Data
public class ReserveStockEvent extends ReceivedEvent {
    private String stockId;
    private long numberOfItemsSold;

}
