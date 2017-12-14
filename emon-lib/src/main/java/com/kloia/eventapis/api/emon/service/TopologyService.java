package com.kloia.eventapis.api.emon.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.AbstractEntryProcessor;
import com.hazelcast.map.listener.EntryExpiredListener;
import com.kloia.eventapis.api.emon.configuration.Components;
import com.kloia.eventapis.api.emon.domain.BaseEvent;
import com.kloia.eventapis.api.emon.domain.ProducedEvent;
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

            operationsMap.executeOnKey(key, new EventTopologyUpdater(
                    eventWrapper.getOpId(), eventWrapper.getSender(),
                    eventWrapper.getAggregateId(), eventWrapper.getOpDate(),
                    baseEvent, targetList, topic));

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

    private static class EventTopologyUpdater extends AbstractEntryProcessor<String, Topology> {


        private String opId;
        private String sender;
        private String aggregateId;
        private long opDate;

        private BaseEvent baseEvent;
        private List<String> targetList;
        private String topic;

        public EventTopologyUpdater(String opId, String sender,
                                    String aggregateId, long opDate,
                                    BaseEvent baseEvent,
                                    List<String> targetList,
                                    String topic) {
            this.opId = opId;
            this.sender = sender;
            this.aggregateId = aggregateId;
            this.opDate = opDate;

            this.baseEvent = baseEvent;
            this.targetList = targetList;
            this.topic = topic;
        }

        @Override
        public Object process(Map.Entry<String, Topology> entry) {
            try {
                Topology topology = entry.getValue();
                if (topology == null) {
                    topology = new Topology(opId);
                }
                if (baseEvent.getEventType() == EventType.OP_START || baseEvent.getEventType() == EventType.OP_SINGLE) {
                    topology.setInitiatorCommand(aggregateId);
                    topology.setInitiatorService(sender);
                    topology.setOpDate(opDate);
                }

                ProducedEvent producedEvent = new ProducedEvent(topic, sender,
                        aggregateId, baseEvent.getEventType(), baseEvent.getSender(), targetList);
                boolean b = topology.attachProducedEvent(producedEvent);
                if (!b)
                    log.warn("We Couldn't attach event:" + producedEvent);
                entry.setValue(topology);
                return topology;
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
                value.attachOperation(operation);
                entry.setValue(value);
                return value;
            } catch (Exception e) {
                log.error("We Couldn't attach Operation:" + e.getMessage());
                return null;
            }
        }
    }
}
