package com.kloia.eventapis.api.emon.domain;

import com.kloia.eventapis.common.Context;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.pojos.TransactionState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by zeldalozdemir on 25/01/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationEvent implements Serializable {


    private static final long serialVersionUID = 3269757297830153667L;
    private TransactionState transactionState;
    private String aggregateId;
    private String sender;
    private long opDate;
    private Context context;

    public OperationEvent(Operation operation) {
        this.transactionState = operation.getTransactionState();
        this.aggregateId = operation.getAggregateId();
        this.sender = operation.getSender();
        this.context = operation.getContext();
        this.opDate = operation.getOpDate() != 0L ? operation.getOpDate() : System.currentTimeMillis();
    }
}
