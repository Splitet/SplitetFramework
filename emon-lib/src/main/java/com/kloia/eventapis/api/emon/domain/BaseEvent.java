package com.kloia.eventapis.api.emon.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kloia.eventapis.common.ReceivedEvent;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseEvent extends ReceivedEvent {
}
