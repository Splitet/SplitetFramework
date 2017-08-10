package com.kloia.sample.dto.event;

import com.kloia.eventapis.pojos.PublishedEvent;
import lombok.Data;

@Data
public class StockReservedEvent extends PublishedEvent{
    private String stockId;
    private String orderId;
    private long numberOfItemsSold;
}
