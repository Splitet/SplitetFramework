package com.kloia.eventapis.api.store.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.AbstractEntryProcessor;
import com.kloia.eventapis.api.store.domain.BaseEvent;
import com.kloia.eventapis.api.store.domain.EventHandler;
import com.kloia.eventapis.api.store.domain.Topology;
import com.kloia.eventapis.common.EventType;
import com.kloia.eventapis.kafka.PublishedEventWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.kloia.eventapis.api.store.configuration.Components.OPERATIONS_MAP_NAME;

@Service
@Slf4j
public class TopologyService implements MessageListener<String, PublishedEventWrapper> {
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
        operationsMap = hazelcastInstance.getMap(OPERATIONS_MAP_NAME);
        this.objectMapper = new ObjectMapper();
        eventReader = objectMapper.readerFor(BaseEvent.class);
    }


    @Override
    public void onMessage(ConsumerRecord<String, PublishedEventWrapper> data) {
        try {
            PublishedEventWrapper eventWrapper = data.value();
            String key = data.key();
            log.info(key + " - " + data.topic() + " - " + eventWrapper.getSender() + " Data:" + data);
            if(data.topic().equals("operation-events"))
                return;
            BaseEvent baseEvent = eventReader.readValue(eventWrapper.getEvent());

            List<String> targetList = topicService.getTopicServiceList().get(data.topic());

            EventHandler eventHandler = new EventHandler(data.topic(), eventWrapper.getSender(), baseEvent.getEventType(), baseEvent.getSender(), targetList);
            if (baseEvent.getEventType() == EventType.OP_SINGLE) {
                operationsMap.put(key, new Topology(eventHandler, eventWrapper.getAggregateId()));
            } else if (baseEvent.getEventType() == EventType.OP_START) {
                operationsMap.put(key, new Topology(eventHandler, eventWrapper.getAggregateId()));
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
                if(!b)
                    log.warn("We Couldn't attach event:"+eventHandler);
                entry.setValue(value);
                return value;
            } catch (Exception e) {
                log.error("We Couldn't attach event:"+e.getMessage());
                return eventHandler;
            }
        }
    }
}
