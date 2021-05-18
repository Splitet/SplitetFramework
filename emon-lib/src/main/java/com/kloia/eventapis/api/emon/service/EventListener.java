package com.kloia.eventapis.api.emon.service;

import com.hazelcast.core.IMap;
import com.kloia.eventapis.api.emon.domain.Topic;
import com.kloia.eventapis.api.emon.service.processor.EndOffsetSetter;
import com.kloia.eventapis.kafka.PublishedEventWrapper;
import com.kloia.eventapis.pojos.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.stereotype.Controller;

import java.io.Serializable;
import java.util.Map;

@Controller
@Slf4j
public class EventListener implements EventMessageListener {

    public static final long TIME_TO_LIVE_SECONDS = 6000;

    @Autowired
    private IMap<String, Topic> topicsMap;


    public void onEventMessage(ConsumerRecord<String, Serializable> record, PublishedEventWrapper eventWrapper) {
        try {
            String topic = record.topic();
            if (topic.equals("operation-events")) {
                log.warn("Topic must not be operation-events");
                return;
            }
            String opId = eventWrapper.getContext().getOpId();
            log.info("opId:" + opId + " EventMessage -> Topic: " + topic
                    + " - Sender: " + eventWrapper.getSender() + " - aggregateId: " + eventWrapper.getContext().getCommandContext());

            topicsMap.submitToKey(topic, new EndOffsetSetter(record.partition(), record.offset() + 1));

        } catch (Exception e) {
            log.error("Error While Handling Event:" + e.getMessage(), e);
        }
    }

    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekAware.ConsumerSeekCallback callback) {
        for (Map.Entry<TopicPartition, Long> entry : assignments.entrySet()) {
            String topic = entry.getKey().topic();
            topicsMap.putIfAbsent(topic, new Topic());
            topicsMap.submitToKey(topic, new EndOffsetSetter(entry.getKey().partition(), entry.getValue()));
        }
        log.warn("onPartitionsAssigned:" + assignments.toString());
    }

    public void onOperationMessage(ConsumerRecord<String, Serializable> record, Operation operation) {
        try {
            log.info("opId: " + record.key() + " OperationMessage -> Topic: " + record.topic() + " - offset:" + record.offset() + " - Sender: "
                    + operation.getSender() + " - aggregateId: " + operation.getAggregateId() + " transactionState:" + operation.getTransactionState());

            topicsMap.submitToKey(Operation.OPERATION_EVENTS, new EndOffsetSetter(record.partition(), record.offset() + 1));
        } catch (Exception e) {
            log.error("Error While Handling Event:" + e.getMessage(), e);
        }
    }

}
