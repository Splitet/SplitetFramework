package com.kloia.eventapis.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Data
@NoArgsConstructor
public class Context {
    private String opId;
    private String parentOpId;
    private String commandContext;
    private long commandTimeout;
    private long startTime;

    @JsonIgnore
    private transient boolean preGenerated = false;
    @JsonIgnore
    private transient List<Consumer<Context>> preGenerationConsumers = new ArrayList<>();

    public Context(String opId) {
        this.opId = opId;
    }

    public void setGenerated() {
        preGenerated = true;
        preGenerationConsumers.forEach(contextConsumer -> contextConsumer.accept(this));
    }

    private long getExpireTime() {
        return startTime + commandTimeout;
    }

    public boolean isEmpty() {
        return opId == null;
    }

}
