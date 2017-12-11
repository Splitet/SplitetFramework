package com.kloia.eventapis.api.store.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kloia.eventapis.common.EventType;
import com.kloia.eventapis.common.PublishedEvent;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseEvent extends PublishedEvent {
    private EventType  eventType;
}
