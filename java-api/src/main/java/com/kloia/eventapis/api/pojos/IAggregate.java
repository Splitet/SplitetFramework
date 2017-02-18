package com.kloia.eventapis.api.pojos;


import com.kloia.eventapis.api.impl.OperationRepository;

/**
 * Created by zeldalozdemir on 30/01/2017.
 */
public interface IAggregate {
    void execute(OperationRepository operationRepository, String... params);
}
/**/