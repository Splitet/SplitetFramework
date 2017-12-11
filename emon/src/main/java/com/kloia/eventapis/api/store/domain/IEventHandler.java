package com.kloia.eventapis.api.store.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.kloia.eventapis.pojos.Operation;
import lombok.Data;

import java.io.Serializable;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME,
        include=JsonTypeInfo.As.PROPERTY,
        property="type")
@JsonSubTypes({
        @JsonSubTypes.Type(value=EventHandler.class, name="event"),
        @JsonSubTypes.Type(value=NoneHandler.class, name="none"),
})
public interface IEventHandler extends Serializable {

    boolean attachHandler(EventHandler eventHandler);

    void incrementNumberOfVisit();

    boolean isFinished();

    boolean attachOperation(Operation operation);

    void setOperation(Operation operation);
}
