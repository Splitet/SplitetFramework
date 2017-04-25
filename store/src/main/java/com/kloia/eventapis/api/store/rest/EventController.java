package com.kloia.eventapis.api.store.rest;

import com.kloia.eventapis.pojos.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteQueue;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.configuration.CollectionConfiguration;
<<<<<<< HEAD
<<<<<<< HEAD
import org.apache.kafka.clients.consumer.ConsumerRecord;
=======
>>>>>>> fe1c6bd...  - Evented DB usage examples
=======
import org.apache.kafka.clients.consumer.ConsumerRecord;
>>>>>>> dc25faf...  - Event bus implementation examples
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by zeldalozdemir on 22/01/2017.
 */
@Slf4j
@RestController
public class EventController {

    private static final String TEMPLATE = "Hello, %s!";

    @RequestMapping("/home")
    public ResponseEntity<?> greeting(
            @RequestParam(value = "name", defaultValue = "World") String name) {
        return ResponseEntity.ok(String.format(TEMPLATE, name));
    }

    @RequestMapping("/req")
    public ResponseEntity<?> reqInterceptor(){
        String url ="http://google.com";
        HttpMethod method = HttpMethod.GET;

        Map<String,String> urlVariable = new HashMap<>();
        urlVariable.put("q", "Concretepage");

        return ResponseEntity.ok().build();
    }

    protected HttpHeaders header() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Autowired
    Ignite ignite;

    @Autowired
    KafkaTemplate kafkaTemplate;

    @KafkaListener( id = "op-listener",topics = "operation-events")
    private void listenOperations(ConsumerRecord<UUID,IOperationEvents> data){
        log.warn("Incoming Message: "+data);
    }


    @PostConstruct
    public void start() {
        log.info("Application is started for Node:" + ignite.cluster().nodes());
        CollectionConfiguration cfg = new CollectionConfiguration();
        IgniteQueue<Object> queue = ignite.queue("main", 0, cfg);
        IgniteCache<UUID, Operation> operationCache = ignite.cache("operationCache");
        log.info("Application is started for KeySizes:" + operationCache.size(CachePeekMode.PRIMARY));
        ArrayList<Event> events = new ArrayList<>();
        events.add(new Event(UUID.randomUUID(), IEventType.EXECUTE, EventState.CREATED, new String[]{"firstpar1","firstpar2"}));
        operationCache.put(UUID.randomUUID(), new Operation("TEST_CREATE", events, TransactionState.RUNNING));
        log.info("Application is started for KeySizes:" + operationCache.size(CachePeekMode.PRIMARY));
/*        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                kafkaTemplate.send("operation-events",UUID.randomUUID(),
                        new Operation("blaaggre",Arrays.asList(new Event()),TransactionState.RUNNING));

            }
        }, 3,3, TimeUnit.SECONDS);*/

//        log.info(transactionCache.get(UUID.fromString("4447a089-e5f7-477c-9807-79210fafa296")).toString());
    }
}