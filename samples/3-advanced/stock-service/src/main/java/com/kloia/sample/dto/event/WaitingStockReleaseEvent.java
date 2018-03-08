package com.kloia.sample.dto.event;

import com.kloia.eventapis.common.ReceivedEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaitingStockReleaseEvent extends ReceivedEvent {
    private String stockId;
    private int reservedStockVersion;
}
