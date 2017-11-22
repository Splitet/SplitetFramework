package com.kloia.eventapis.api.store.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

@Data
@JsonTypeName("none")
public class NoneHandler implements IEventHandler {
    public static final NoneHandler noneHandler = new NoneHandler();

    private NoneHandler() {
    }

    @Override
    public boolean attachHandler(EventHandler eventHandler) {
        return false;
    }

    @Override
    public void incrementNumberOfVisit() {

    }
}
