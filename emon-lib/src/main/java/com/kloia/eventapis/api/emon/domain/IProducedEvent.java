package com.kloia.eventapis.api.emon.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.kloia.eventapis.pojos.Operation;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ProducedEvent.class, name = "event")
})
public interface IProducedEvent extends Serializable {

    boolean attachHandler(ProducedEvent eventHandler);

    void incrementNumberOfVisit();

    boolean isFinished();

    boolean attachOperation(OperationEvent operation);

    void setOperation(OperationEvent operation);
}
