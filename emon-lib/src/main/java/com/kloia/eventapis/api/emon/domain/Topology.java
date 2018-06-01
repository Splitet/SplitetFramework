package com.kloia.eventapis.api.emon.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.kloia.eventapis.api.emon.service.TopologyService;
import com.kloia.eventapis.pojos.TransactionState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
@Slf4j
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"opId", "parentOpId", "initiatorService", "initiatorCommand", "startTime", "endTime", "commandTimeout", "commandExecutionTime", "timedOut",
        "finished", "operation", "operationState", "unassignedOperations", "unassignedEvents", "producedEvents"})
public class Topology implements Serializable {

    private String parentOpId;
    private String opId;
    private String initiatorService;
    private String initiatorCommand;
    private long startTime;
    private long endTime;
    private long commandTimeout;
    private OperationEvent operation;
    private TransactionState operationState = TransactionState.RUNNING;
    private Set<OperationEvent> unassignedOperations = new HashSet<>();
    private Set<ProducedEvent> unassignedEvents = new HashSet<>();
    private Set<ProducedEvent> producedEvents = new HashSet<>();

    public Topology() {
    }

    public Topology(String opId, String parentOpId) {
        this.opId = opId;
        this.parentOpId = parentOpId;
    }

    public Topology(String opId, String parentOpId, ProducedEvent head, String initiatorCommand) {
        this.opId = opId;
        this.parentOpId = parentOpId;
        this.producedEvents.add(head);
        this.initiatorService = head.getSender();
        this.initiatorCommand = initiatorCommand;
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
        Set<OperationEvent> toConsume = this.unassignedOperations;
        this.unassignedOperations = new HashSet<>();
        log.debug("Trying to Consume stales:" + toConsume);
        toConsume.forEach(this::attachOperation);
    }

    public boolean isFinished() {
        return operationState != TransactionState.RUNNING && producedEvents.stream().allMatch(ProducedEvent::isFinished);
    }

    public long getCommandExecutionTime() {
        return endTime - startTime;
    }

    public boolean isTimedOut() {
        return endTime - startTime > (commandTimeout + TopologyService.GRACE_PERIOD_IN_MILLIS);
    }

    public void attachOperation(OperationEvent operation) {
        if (operationState != TransactionState.RUNNING)
            log.error("Topology is Already ended with State:" + operationState + " New Operation: " + operation);
        this.endTime = operation.getOpDate();
        if (Objects.equals(operation.getSender(), getInitiatorService()) && Objects.equals(operation.getAggregateId(), getInitiatorCommand())) {
            operationState = operation.getTransactionState();
            this.operation = operation;
        } else {
            boolean result = producedEvents.stream().anyMatch(producedEvent -> producedEvent.attachOperation(operation));
            if (result)
                operationState = operation.getTransactionState();
            else {
                log.debug("We Couldn't attach, " + opId + " Adding to UnAssigned Operation Event:" + operation);
                unassignedOperations.add(operation);
            }
        }
    }
}
