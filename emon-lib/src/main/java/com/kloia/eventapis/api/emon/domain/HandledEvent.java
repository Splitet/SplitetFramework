package com.kloia.eventapis.api.emon.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kloia.eventapis.common.EventType;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.pojos.TransactionState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.DeclareAnnotation;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Data
@JsonTypeName("handled")
public class HandledEvent implements IHandledEvent {

    private String handlerService;
    private String topic;
    private Operation operation;
    private Set<ProducedEvent> producedEvents = new HashSet<>();

    public HandledEvent(ProducedEvent producedEvent, String handlerService, String topic) {
        this.handlerService = handlerService;
        this.topic = topic;
        producedEvents.add(producedEvent);
    }


    public void attachProducedEvent(ProducedEvent producedEvent) {
        Optional<ProducedEvent> first = producedEvents.stream().filter(existingEvent -> Objects.equals(existingEvent.getTopic(), producedEvent.getTopic())).findFirst();
        if(first.isPresent())
            first.get().incrementNumberOfVisit();
        else
            producedEvents.add(producedEvent);
        log.info("Duplicate Event Handle for:" + producedEvent);
    }

    @Override
    public boolean isFinished() {
        return producedEvents.stream().allMatch(ProducedEvent::isFinished);
    }

    @Override
    public boolean attachOperation(Operation operation) {
        if (Objects.equals(operation.getSender(), getHandlerService())
                && Objects.equals(operation.getAggregateId(), topic)
                && operation.getTransactionState() == TransactionState.TXN_SUCCEDEED) {
            this.operation = operation;
            return true;
        }
        return producedEvents.stream().anyMatch(producedEvent -> producedEvent.attachOperation(operation));
    }
}
