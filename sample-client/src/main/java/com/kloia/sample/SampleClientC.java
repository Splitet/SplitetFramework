package com.kloia.sample;

import com.kloia.eventapis.StoreApi;
import com.kloia.eventapis.impl.AggregateBuilder;
import com.kloia.eventapis.impl.EventRepository;
import com.kloia.eventapis.pojos.Aggregate;
import com.kloia.eventapis.pojos.Event;
import com.kloia.eventapis.pojos.IAggregate;

import java.util.Arrays;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by zeldalozdemir on 30/01/2017.
 */

public class SampleClientC {

    public static void main(String[] args) {

        StoreApi storeApi = StoreApi.createStoreApi("127.0.0.1:7500");

        EventRepository eventRepository = storeApi.getEventRepository();
        AggregateBuilder aggregateBuilder = storeApi.getAggregateBuilder();


        Aggregate aggregate = aggregateBuilder.createAggregate(new IAggregate() {
            public void execute(EventRepository eventRepository, String... params) {
                System.out.println("Executing: C" + Arrays.toString(params));
            }
        });

        eventRepository.registerForEvent(aggregate, "UPDATE_EVENT_C");


//        aggregate.execute("First", "Event");

        LockSupport.park();
    }
}
