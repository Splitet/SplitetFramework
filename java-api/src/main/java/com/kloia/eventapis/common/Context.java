package com.kloia.eventapis.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Data
@NoArgsConstructor
@SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
public class Context implements Serializable {

    private static final long serialVersionUID = 7165801319573687119L;

    private String opId;
    private String parentOpId;
    private String commandContext;
    private long commandTimeout;
    private long startTime;

    @JsonIgnore
    private transient boolean preGenerated;
    @JsonIgnore
    private transient List<Consumer<Context>> preGenerationConsumers = new ArrayList<>();

    public Context(String opId) {
        this.opId = opId;
    }

    public void setGenerated() {
        preGenerated = false;
        preGenerationConsumers.forEach(contextConsumer -> contextConsumer.accept(this));
    }
}
