package com.kloia.eventapis.api.emon.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.pojos.TransactionState;
import lombok.Data;

@Data
@JsonTypeName("none")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NoneHandled implements IHandledEvent {

    private Operation operation;

    NoneHandled() {
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
