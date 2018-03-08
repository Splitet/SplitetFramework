package com.kloia.eventapis.common;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Context {
    private String opId;
    private String parentOpId;
    private String commandContext;
    private boolean preGenerated = false;

    public Context(String opId) {
        this.opId = opId;
    }

    public boolean isEmpty() {
        return opId == null;
    }

}
