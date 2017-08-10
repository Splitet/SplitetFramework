package com.kloia.sample.dto.event;

import com.kloia.eventapis.pojos.PublishedEvent;
import lombok.Data;

@Data
public class ReserveStockEvent extends PublishedEvent {
    private String stockId;
    private long numberOfItemsSold;
}
