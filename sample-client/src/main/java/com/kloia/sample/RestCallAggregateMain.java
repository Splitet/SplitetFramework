package com.kloia.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

/**
 * Created by zeldalozdemir on 30/01/2017.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.kloia.sample","com.kloia.evented"})
@EnableCassandraRepositories(  basePackages = "com.kloia.evented")
public class RestCallAggregateMain {

    public static void main(String[] args) {

        SpringApplication.run(RestCallAggregateMain.class, args);

/*

        StoreApi storeApi = StoreApi.createStoreApi("127.0.0.1:7500");

        EventRepository eventRepository = storeApi.getEventRepository();
        AggregateBuilder aggregateBuilder = storeApi.getAggregateBuilder();

        String path = "/gapi/order";
        String method = "POST";
        Object dto = new OrderCreateDTO();
//        Map<String,String> queryParams = Collections.singletonMap("queryparam1","6");
        Map<String, String> headers = Collections.singletonMap("opid", "4447a089-e5f7-477c-9807-79210fafa296");


        // Interceptor
        String eventName = "order";
        String eventType = method; // POST
        String mainAggregateName = eventName + "_" + eventType;
        // in Case Rest
        UUID opid = headers.containsKey("opid") ? UUID.fromString(headers.get("opid")) : null;
        Operation operation = eventRepository.getOrCreateOperation(mainAggregateName, opid);

        eventRepository.publishEvent(new Event(opid,UUID.randomUUID(), IEventType.EXECUTE, EventState.CREATED, args));

*/



    }

}