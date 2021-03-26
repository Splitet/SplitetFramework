package io.splitet.core.common;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface RecordedEvent {
    @JsonIgnore
    default String getEventName() {
        return this.getClass().getSimpleName();
    }
}
