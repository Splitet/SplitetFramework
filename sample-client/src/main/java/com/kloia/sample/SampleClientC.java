package com.kloia.sample;

import com.kloia.eventapis.api.StoreApi;
import com.kloia.eventapis.api.impl.AggregateBuilder;
import com.kloia.eventapis.api.impl.OperationRepository;
import com.kloia.eventapis.api.pojos.Aggregate;
import com.kloia.eventapis.api.pojos.IAggregate;

import java.util.Arrays;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by zeldalozdemir on 30/01/2017.
 */

public class SampleClientC {

/*    public static void main(String[] args) {

        StoreApi storeApi = StoreApi.createStoreApi("127.0.0.1:7500");

        OperationRepository operationRepository = storeApi.getOperationRepository();
        AggregateBuilder aggregateBuilder = storeApi.getAggregateBuilder();


        Aggregate aggregate = aggregateBuilder.createAggregate(new IAggregate() {
            public void execute(OperationRepository eventRepository, String... params) {
                System.out.println("Executing: C" + Arrays.toString(params));
            }
        });

        operationRepository.registerForEvent(aggregate, "UPDATE_EVENT_C");


//        aggregate.execute("First", "Event");

        LockSupport.park();
    }*/
}
