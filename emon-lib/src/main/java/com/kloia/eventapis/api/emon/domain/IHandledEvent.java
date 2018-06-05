package com.kloia.eventapis.api.emon.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = HandledEvent.class, name = "handled"),
        @JsonSubTypes.Type(value = NoneHandled.class, name = "none")
})
public interface IHandledEvent extends Serializable {
    boolean isFinished();

    void setOperation(OperationEvent operation);

    boolean attachOperation(OperationEvent operationToAttach);
}
