package com.kloia.eventapis;

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
public class SampleClientA {

    public static void main(String[] args) {

        StoreApi storeApi = StoreApi.createStoreApi("127.0.0.1:7500");

        EventRepository eventRepository = storeApi.getEventRepository();
        AggregateBuilder aggregateBuilder = storeApi.getAggregateBuilder();
//        AggregateBuilder aggregateBuilder = new AggregateBuilder(eventRepository);


        Aggregate aggregate = aggregateBuilder.createAggregate(new IAggregate() {
            public void execute(EventRepository eventRepository, String... params) {
                System.out.println("Executing A:" + Arrays.toString(params));
                eventRepository.sendEvent("UPDATE_EVENT_B", params);
                eventRepository.sendEvent("UPDATE_EVENT_C", params);
            }
        });

        eventRepository.registerForEvent(aggregate, "UPDATE_EVENT_A", "INSERT_EVENT_XA");


        aggregate.execute(  "First", "Event");
        LockSupport.park();
    }
}