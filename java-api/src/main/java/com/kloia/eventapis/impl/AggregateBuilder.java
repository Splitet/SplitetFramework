package com.kloia.eventapis.impl;

import com.kloia.eventapis.pojos.Aggregate;
import com.kloia.eventapis.pojos.IAggregate;

/**
 * Created by zeldalozdemir on 30/01/2017.
 */
public class AggregateBuilder {
    private OperationRepository operationRepository;

    public AggregateBuilder(OperationRepository operationRepository) {
        this.operationRepository = operationRepository;
    }

    public Aggregate createAggregate(IAggregate aggregate) {
        return new Aggregate(operationRepository, aggregate);
    }


}
