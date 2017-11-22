package com.kloia.eventapis.api.store.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.kloia.eventapis.api.store.domain.BaseEvent;
import com.kloia.eventapis.api.store.domain.Topology;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.pojos.IOperationEvents;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.kloia.eventapis.api.store.configuration.Components.OPERATIONS_MAP_NAME;

/**
 * Created by zeldalozdemir on 22/01/2017.
 */
@Slf4j
@RestController
@RequestMapping(value = "/operations/v1/")
public class EventController {


    @Autowired
    @Qualifier("hazelcastInstance")
    private HazelcastInstance hazelcastInstance;

    private IMap<String, Topology> operationsMap;

    @PostConstruct
    public void init() {
        operationsMap = hazelcastInstance.getMap(OPERATIONS_MAP_NAME);
    }

    @RequestMapping(value = "/{opId}", method = RequestMethod.GET)
    public ResponseEntity<?> getOperation(@PathVariable("opId") String opId) throws IOException, EventStoreException {
        return new ResponseEntity<Object>(operationsMap.get(opId), HttpStatus.OK);
    }


}