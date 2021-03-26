package io.splitet.sample.dto.event;

import io.splitet.core.common.EventType;
import io.splitet.core.common.PublishedEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaitingStockReleaseEvent extends PublishedEvent {
    private String stockId;
    private int reservedStockVersion;

    @Override
    public EventType getEventType() {
        return EventType.OP_START;
    }
}
