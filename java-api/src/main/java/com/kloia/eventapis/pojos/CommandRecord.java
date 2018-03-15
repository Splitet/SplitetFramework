package com.kloia.eventapis.pojos;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.kloia.eventapis.common.RecordedEvent;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class CommandRecord implements RecordedEvent {
    private Map<Integer, ?> parameters = new HashMap<>();
    private String eventName;

    @JsonGetter("parameters")
    public Map<Integer, Object> getParameters() {
        return (Map<Integer, Object>) parameters;
    }

    @JsonSetter("parameters")
    public void setParameters(Map<Integer, JsonNode> parameters) {
        this.parameters = parameters;
    }
}
