package com.kloia.sample.dto.event;

import com.kloia.eventapis.common.ReceivedEvent;
import lombok.Data;

@Data
public class ReserveStockEvent extends ReceivedEvent {
    private String stockId;
    private long numberOfItemsSold;

}
