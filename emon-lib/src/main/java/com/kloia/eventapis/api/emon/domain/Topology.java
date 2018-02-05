package com.kloia.eventapis.api.emon.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.pojos.TransactionState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Topology implements Serializable {

    private String opId;
    private String initiatorService;
    private String initiatorCommand;
    private long opDate;
    private Operation operation;
    private TransactionState operationState = TransactionState.RUNNING;
    private Set<Operation> unassignedOperations = new HashSet<>();
    private Set<ProducedEvent> unassignedEvents = new HashSet<>();
    private Set<ProducedEvent> producedEvents = new HashSet<>();

    public Topology(String opId) {
        this.opId = opId;
    }

    public Topology(String opId, ProducedEvent head, String initiatorCommand, long opDate) {
        this.opId = opId;
        this.producedEvents.add(head);
        this.initiatorService = head.getSender();
        this.initiatorCommand = initiatorCommand;
        this.opDate = opDate;
    }

    public boolean attachProducedEvent(ProducedEvent producedEvent) {
        boolean result;
        if (Objects.equals(producedEvent.getAggregateId(), initiatorCommand) && Objects.equals(producedEvent.getSender(), initiatorService)) {
            result = producedEvents.add(producedEvent);
        } else
            result = producedEvents.stream().anyMatch(existingEvent -> existingEvent.attachHandler(producedEvent));
        if (result)
            consumeStales();
        else
            unassignedEvents.add(producedEvent);
        return result;
    }

    private void consumeStales() {
        if (unassignedOperations.isEmpty())
            return;
        Set<Operation> toConsume = this.unassignedOperations;
        this.unassignedOperations = new HashSet<>();
        log.warn("Trying to Consume stales:" + toConsume);
        toConsume.forEach(this::attachOperation);
    }

    public boolean isFinished() {
        return operationState != TransactionState.RUNNING && producedEvents.stream().allMatch(ProducedEvent::isFinished);
    }

    public void attachOperation(Operation operation) {
        if (operationState != TransactionState.RUNNING)
            log.error("Topology is Already ended with State:" + operationState + " New Operation: " + operation);
        if (Objects.equals(operation.getSender(), getInitiatorService()) && Objects.equals(operation.getAggregateId(), getInitiatorCommand())) {
            operationState = operation.getTransactionState();
            this.operation = operation;
        } else {
            boolean result = producedEvents.stream().anyMatch(producedEvent -> producedEvent.attachOperation(operation));
            if (result)
                operationState = operation.getTransactionState();
            else {
                log.warn("We Couldn't attach, Adding to UnAssigned Operation Event:" + operation);
                unassignedOperations.add(operation);
            }
        }
    }
}
