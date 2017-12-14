package com.kloia.eventapis.api.emon.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.kloia.eventapis.pojos.Operation;

import java.io.Serializable;


@JsonTypeInfo(use=JsonTypeInfo.Id.NAME,
        include=JsonTypeInfo.As.PROPERTY,
        property="type")
@JsonSubTypes({
        @JsonSubTypes.Type(value=HandledEvent.class, name="handled"),
        @JsonSubTypes.Type(value=NoneHandled.class, name="none"),
})
public interface IHandledEvent extends Serializable{
    boolean isFinished();

    void setOperation(Operation operation);

    boolean attachOperation(Operation operation);
}
