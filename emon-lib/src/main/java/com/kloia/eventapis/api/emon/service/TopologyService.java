package com.kloia.eventapis.api.emon.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.AbstractEntryProcessor;
import com.kloia.eventapis.api.emon.domain.BaseEvent;
import com.kloia.eventapis.api.emon.domain.ProducedEvent;
import com.kloia.eventapis.api.emon.domain.Topic;
import com.kloia.eventapis.api.emon.domain.Topology;
import com.kloia.eventapis.common.Context;
import com.kloia.eventapis.common.EventKey;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TopologyService implements MessageListener<String, Serializable> {
    public static final int GRACE_PERIOD_IN_MILLIS = 3000;
    @Autowired
    @Qualifier("hazelcastInstance")
    private HazelcastInstance hazelcastInstance;

    @Autowired
    private IMap<String,Topic> topicsMap;

    @Autowired
    private IMap<String, Topology> operationsMap;

    @Autowired
    private ObjectMapper objectMapper;

    private ObjectReader eventReader;

    private static long calculateTimeout(Context context) {
        long diff = System.currentTimeMillis() - context.getStartTime();
        return context.getCommandTimeout() + GRACE_PERIOD_IN_MILLIS;
    }

    @PostConstruct
    public void init() {
        eventReader = objectMapper.readerFor(BaseEvent.class);
    }

    private void onEventMessage(String topic, String key, PublishedEventWrapper eventWrapper) {
        try {
            log.info(topic + " - " + eventWrapper.getSender() + " - " + key + " - " + eventWrapper.toString());
            if (topic.equals("operation-events"))
                return;
            BaseEvent baseEvent = eventReader.readValue(eventWrapper.getEvent());

            List<String> targetList = new ArrayList<>(topicsMap.get(topic).getServiceDataHashMap().keySet());

            operationsMap.putIfAbsent(key,
                    new Topology(eventWrapper.getContext().getOpId(), eventWrapper.getContext().getParentOpId()),
                    calculateTimeout(eventWrapper.getContext()), TimeUnit.MILLISECONDS);
            operationsMap.executeOnKey(key, new EventTopologyUpdater(
                    eventWrapper, baseEvent.getEventType(), baseEvent.getSender(), targetList, topic));

        } catch (IOException e) {
            log.error("Error While Handling Event:" + e.getMessage(), e);
        }
    }

    private void onOperationMessage(String key, Operation operation) {
        try {
            log.info(key + " - " + operation.getSender() + " Data:" + operation);
            operationsMap.putIfAbsent(key,
                    new Topology(key, operation.getParentId()),
                    calculateTimeout(operation.getContext()), TimeUnit.MILLISECONDS);
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


        private EventType eventType;
        private EventKey senderEventKey;

        private List<String> targetList;
        private String topic;
        private PublishedEventWrapper eventWrapper;

        public EventTopologyUpdater(PublishedEventWrapper eventWrapper,
                                    EventType eventType,
                                    EventKey senderEventKey,
                                    List<String> targetList,
                                    String topic) {
            this.eventWrapper = eventWrapper;
            this.eventType = eventType;
            this.senderEventKey = senderEventKey;

            this.targetList = targetList;
            this.topic = topic;
        }

        @Override
        public Object process(Map.Entry<String, Topology> entry) {
            try {
                Topology topology = entry.getValue();
                if (topology == null) {
                    topology = new Topology(eventWrapper.getContext().getOpId(), eventWrapper.getContext().getParentOpId());
                }
                if (eventType == EventType.OP_START || eventType == EventType.OP_SINGLE) {
                    topology.setInitiatorCommand(eventWrapper.getContext().getCommandContext());
                    topology.setInitiatorService(eventWrapper.getSender());
                    topology.setCommandTimeout(calculateTimeout(eventWrapper.getContext()));
                    topology.setStartTime(eventWrapper.getContext().getStartTime());
                }

                ProducedEvent producedEvent = new ProducedEvent(topic, eventWrapper.getSender(),
                        eventWrapper.getContext().getCommandContext(), eventType, senderEventKey, targetList, eventWrapper.getOpDate());
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
                    value = new Topology(entry.getKey(), operation.getParentId());
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
