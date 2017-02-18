package com.kloia.eventapis.api.impl;

import com.kloia.eventapis.api.pojos.Aggregate;
import com.kloia.eventapis.api.pojos.IAggregate;

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
