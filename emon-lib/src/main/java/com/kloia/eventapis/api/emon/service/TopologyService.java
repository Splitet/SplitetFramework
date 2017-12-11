package com.kloia.eventapis.api.emon.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.AbstractEntryProcessor;
import com.hazelcast.map.listener.EntryExpiredListener;
import com.kloia.eventapis.api.emon.configuration.Components;
import com.kloia.eventapis.api.emon.domain.BaseEvent;
import com.kloia.eventapis.api.emon.domain.EventHandler;
import com.kloia.eventapis.api.emon.domain.Topology;
import com.kloia.eventapis.common.EventType;
import com.kloia.eventapis.kafka.PublishedEventWrapper;
import com.kloia.eventapis.pojos.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TopologyService implements MessageListener<String, Serializable> {
    @Autowired
    @Qualifier("hazelcastInstance")
    private HazelcastInstance hazelcastInstance;
    @Autowired
    private TopicService topicService;

    private IMap<String, Topology> operationsMap;
    private ObjectMapper objectMapper;
    private ObjectReader eventReader;


    @PostConstruct
    public void init() {
        operationsMap = hazelcastInstance.getMap(Components.OPERATIONS_MAP_NAME);
        operationsMap.addEntryListener((EntryExpiredListener<String, Topology>) event -> {
            event.getKey();
            Topology topology = event.getOldValue();
            if (!topology.isFinished()) {
                log.error("Topology Doesn't Finished:" + topology.toString());
            } else
                log.info("Topology OK:" + topology.toString());

        }, true);
        this.objectMapper = new ObjectMapper();
        eventReader = objectMapper.readerFor(BaseEvent.class);
    }


    private void onEventMessage(String topic, String key, PublishedEventWrapper eventWrapper) {
        try {
            log.info(topic + " - " + eventWrapper.getSender() + " - " + key + " - " + eventWrapper.toString());
            if (topic.equals("operation-events"))
                return;
            BaseEvent baseEvent = eventReader.readValue(eventWrapper.getEvent());

            List<String> targetList = topicService.getTopicServiceList().get(topic);

            EventHandler eventHandler = new EventHandler(topic, eventWrapper.getSender(), baseEvent.getEventType(), baseEvent.getSender(), targetList);
            if (baseEvent.getEventType() == EventType.OP_SINGLE) {
                operationsMap.put(key, new Topology(key, eventHandler, eventWrapper.getAggregateId(), eventWrapper.getOpDate()), 300, TimeUnit.MINUTES);
            } else if (baseEvent.getEventType() == EventType.OP_START) {
                operationsMap.put(key, new Topology(key, eventHandler, eventWrapper.getAggregateId(), eventWrapper.getOpDate()), 300, TimeUnit.MINUTES);
            } else if (baseEvent.getEventType() == EventType.EVENT) {
                operationsMap.executeOnKey(key, new TopologyUpdater(eventHandler));
            } else if (baseEvent.getEventType() == EventType.OP_SUCCESS) {
                operationsMap.executeOnKey(key, new TopologyUpdater(eventHandler));
            } else if (baseEvent.getEventType() == EventType.OP_FAIL) {
                operationsMap.executeOnKey(key, new TopologyUpdater(eventHandler));
            }
        } catch (IOException e) {
            log.error("Error While Handling Event:" + e.getMessage(), e);
        }
    }

    private void onOperationMessage(String key, Operation operation) {
        try {
            log.info(key + " - " + operation.getSender() + " Data:" + operation);
            operationsMap.executeOnKey(key, new OperationTopologyUpdater(operation));
        } catch (Exception e) {
            log.error("Error While Handling Event:" + e.getMessage(), e);
        }
    }

    @Override
    public void onMessage(ConsumerRecord<String, Serializable> data) {
        if (data.value() instanceof PublishedEventWrapper)
            onEventMessage(data.topic(), data.key(), (PublishedEventWrapper) data.value());
        else if (data.value() instanceof Operation)
            onOperationMessage(data.key(), (Operation) data.value());
    }


    private static class TopologyUpdater extends AbstractEntryProcessor<String, Topology> {

        private EventHandler eventHandler;

        public TopologyUpdater(EventHandler eventHandler) {
            this.eventHandler = eventHandler;
        }

        @Override
        public Object process(Map.Entry<String, Topology> entry) {
            try {
                Topology value = entry.getValue();
                if (value == null)
                    throw new RuntimeException("There is no Operation Start");
                boolean b = value.putNextEventHandler(eventHandler);
                if (!b)
                    log.warn("We Couldn't attach event:" + eventHandler);
                entry.setValue(value);
                return value;
            } catch (Exception e) {
                log.error("We Couldn't attach event:" + e.getMessage());
                return null;
            }
        }
    }

    private static class OperationTopologyUpdater extends AbstractEntryProcessor<String, Topology> {

        private Operation operation;

        public OperationTopologyUpdater(Operation operation) {
            this.operation = operation;
        }

        @Override
        public Object process(Map.Entry<String, Topology> entry) {
            try {
                Topology value = entry.getValue();
                if (value == null) {
                    log.warn("There is no Topology with key: " + entry.getKey());
                    value = new Topology(entry.getKey());
                }
                value.putOperation(operation);
                entry.setValue(value);
                return value;
            } catch (Exception e) {
                log.error("We Couldn't attach Operation:" + e.getMessage());
                return null;
            }
        }
    }
}
