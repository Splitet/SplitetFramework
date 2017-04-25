package com.kloia.eventapis.pojos;

import lombok.Data;

/**
 * Created by zeldalozdemir on 20/04/2017.
 */
@Data
public class AppendEventToOperation implements IOperationEvents {
    private Event event;

    public AppendEventToOperation() {
    }

    public AppendEventToOperation(Event event) {

        this.event = event;
    }
}
