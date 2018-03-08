package com.kloia.sample.dto.event;

import com.kloia.eventapis.common.ReceivedEvent;
import lombok.Data;

@Data
public class StockReservedEvent extends ReceivedEvent {
    private String stockId;
    private String orderId;
    private long numberOfItemsSold;
}
