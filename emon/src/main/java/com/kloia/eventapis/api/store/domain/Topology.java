package com.kloia.eventapis.api.store.domain;

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
public class Topology implements Serializable {

    private String opId;
    private String initiatorService;
    private String initiatorCommand;
    private long opDate;
    private TransactionState transactionState;
    private TransactionState operationState = TransactionState.RUNNING;
    private Set<Operation> unassignedOperations = new HashSet<>();
    private EventHandler head;

    public Topology(String opId) {
        this.opId = opId;
    }

    public Topology(String opId, EventHandler head, String initiatorCommand, long opDate) {
        this.opId = opId;
        this.head = head;
        this.initiatorService = head.getSender();
        this.initiatorCommand = initiatorCommand;
        this.opDate = opDate;
    }

    public boolean putNextEventHandler(EventHandler eventHandler) {
        boolean result = head.attachHandler(eventHandler);
        if (result)
            consumeStales();
        return result;
    }

    private void consumeStales() {
        if (unassignedOperations.isEmpty())
            return;
        Set<Operation> toConsume = this.unassignedOperations;
        this.unassignedOperations = new HashSet<>();
        log.warn("Trying to Consume stales:" + toConsume);
        toConsume.forEach(this::putOperation);
    }

    public boolean isFinished() {
        return head.isFinished();
    }

    public void putOperation(Operation operation) {
        if (operationState != TransactionState.RUNNING)
            log.error("Topology is Already ended with State:" + operationState + " New Operation: " + operation);
        if (Objects.equals(operation.getSender(), getInitiatorService()) && Objects.equals(operation.getAggregateId(), getInitiatorCommand())) {
            operationState = transactionState = operation.getTransactionState();
        } else {
            boolean result = head != null && head.attachOperation(operation);
            if (result)
                operationState = operation.getTransactionState();
            else {
                log.warn("We Couldn't attach, Adding to UnAssigned Operation Event:" + operation);
                unassignedOperations.add(operation);
            }
        }
    }
}
