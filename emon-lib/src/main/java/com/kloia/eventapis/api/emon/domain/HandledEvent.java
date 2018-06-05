package com.kloia.eventapis.api.emon.domain;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kloia.eventapis.pojos.TransactionState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Data
@JsonTypeName("handled")
@JsonPropertyOrder({"handlerService", "topic","finished", "operation", "producedEvents"})
public class HandledEvent implements IHandledEvent {

    private String handlerService;
    private String topic;
    private OperationEvent operation;
    private Set<ProducedEvent> producedEvents = new HashSet<>();

    public HandledEvent(ProducedEvent producedEvent, String handlerService, String topic) {
        this.handlerService = handlerService;
        this.topic = topic;
        producedEvents.add(producedEvent);
    }


    public void attachProducedEvent(ProducedEvent producedEvent) {
        Optional<ProducedEvent> first = producedEvents.stream().filter(existingEvent -> Objects.equals(existingEvent.getTopic(), producedEvent.getTopic())).findFirst();
        if (first.isPresent()) {
            first.get().incrementNumberOfVisit();
            log.info("Duplicate Event Handle for:" + producedEvent);
        }
        else {
            producedEvents.add(producedEvent);
        }
    }

    @Override
    public boolean isFinished() {
        return producedEvents.stream().allMatch(ProducedEvent::isFinished);
    }

    @Override
    public boolean attachOperation(OperationEvent operationToAttach) {
        if (Objects.equals(operationToAttach.getSender(), getHandlerService())
                && Objects.equals(operationToAttach.getAggregateId(), topic)
                && operationToAttach.getTransactionState() == TransactionState.TXN_SUCCEEDED) {
            this.operation = operationToAttach;
            return true;
        }
        return producedEvents.stream().anyMatch(producedEvent -> producedEvent.attachOperation(operationToAttach));
    }
}
