package com.kloia.sample.dto.event;

import com.kloia.eventapis.common.PublishedEvent;
import lombok.Data;

@Data
public class StockNotEnoughEvent extends PublishedEvent{
    private String orderId;
    private long numberOfItemsSold;
}
