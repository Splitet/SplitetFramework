package com.kloia.eventapis.api.emon.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.hazelcast.core.IMap;
import com.hazelcast.map.AbstractEntryProcessor;
import com.kloia.eventapis.api.emon.domain.BaseEvent;
import com.kloia.eventapis.api.emon.domain.OperationEvent;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(value = "emon.listenTopology.enabled", havingValue = "true")
public class TopologyService implements EventMessageListener {
    public static final int GRACE_PERIOD_IN_MILLIS = 3000;

    @Autowired
    private IMap<String, Topic> topicsMap;

    @Autowired
    private IMap<String, Topology> operationsMap;

    @Autowired
    private ObjectMapper objectMapper;

    private ObjectReader eventReader;

    private static long calculateTimeout(Context context) {
        return context.getCommandTimeout() + GRACE_PERIOD_IN_MILLIS;
    }

    @PostConstruct
    public void init() {
        eventReader = objectMapper.readerFor(BaseEvent.class);
    }

    public void onEventMessage(ConsumerRecord<String, Serializable> record, PublishedEventWrapper eventWrapper) {
        try {
            String topic = record.topic();
            String key = record.key();
            log.info("Event: " + topic + " Key:" + key + " Event:" + eventWrapper.toString());
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

    public void onOperationMessage(ConsumerRecord<String, Serializable> record, Operation operation) {
        try {
            log.info("Operation:" + record.key() + " - " + operation.toString());
            operationsMap.putIfAbsent(record.key(),
                    new Topology(record.key(), operation.getParentId()),
                    calculateTimeout(operation.getContext()), TimeUnit.MILLISECONDS);
            operationsMap.executeOnKey(record.key(), new OperationTopologyUpdater(operation));
        } catch (Exception e) {
            log.error("Error While Handling Event:" + e.getMessage(), e);
        }
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
                    log.debug("We Couldn't attach event:" + producedEvent);
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
                    log.info("There is no Topology with key: " + entry.getKey() + " Operation has arrived before events");
                    value = new Topology(entry.getKey(), operation.getParentId());
                }
                value.attachOperation(new OperationEvent(operation));
                entry.setValue(value);
                return value;
            } catch (Exception e) {
                log.error("We Couldn't attach Operation:" + e.getMessage());
                return null;
            }
        }
    }

}
