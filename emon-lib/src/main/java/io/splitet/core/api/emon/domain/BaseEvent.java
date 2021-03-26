package io.splitet.core.api.emon.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.splitet.core.common.ReceivedEvent;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseEvent extends ReceivedEvent {
}
