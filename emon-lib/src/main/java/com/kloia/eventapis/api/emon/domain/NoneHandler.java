package com.kloia.eventapis.api.emon.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.pojos.TransactionState;
import lombok.Data;

@Data
@JsonTypeName("none")
public class NoneHandler implements IEventHandler {

    private Operation operation;

    NoneHandler() {
    }

    @Override
    public boolean attachHandler(EventHandler eventHandler) {
        return false;
    }

    @Override
    public void incrementNumberOfVisit() {

    }

    @Override
    public boolean isFinished() {
        return operation != null && operation.getTransactionState() == TransactionState.TXN_FAILED;
    }

    @Override
    public boolean attachOperation(Operation operation) {
        return false;
    }

    @Override
    public void setOperation(Operation operation) {
        this.operation = operation;
    }
}
