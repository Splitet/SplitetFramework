package com.kloia.eventapis.api.emon.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kloia.eventapis.pojos.TransactionState;
import lombok.Data;

@Data
@JsonTypeName("none")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NoneHandled implements IHandledEvent {

    private OperationEvent operation;
    private boolean finishedAsLeaf = false;

    NoneHandled() {
    }

    @Override
    public boolean isFinished() {
        if (operation != null)
            return operation.getTransactionState() == TransactionState.TXN_FAILED;
        else return finishedAsLeaf;
    }

    @Override
    public boolean attachOperation(OperationEvent operationToAttach) {
        return false;
    }

    @Override
    public void setOperation(OperationEvent operation) {
        this.operation = operation;
    }
}

