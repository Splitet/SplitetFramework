package io.splitet.core.api.emon.service;

import io.splitet.core.kafka.PublishedEventWrapper;
import io.splitet.core.pojos.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public class MultipleEventMessageListener implements EventMessageListener {
    private final List<EventMessageListener> eventMessageListeners;

    public MultipleEventMessageListener(List<EventMessageListener> eventMessageListeners) {
        this.eventMessageListeners = eventMessageListeners;
    }

    public void applyForEach(Consumer<EventMessageListener> listenerConsumer) {
        eventMessageListeners.forEach(eventMessageListener -> {
            try {
                listenerConsumer.accept(eventMessageListener);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    @Override
    public void onOperationMessage(ConsumerRecord<String, Serializable> record, Operation value) {
        applyForEach(eventMessageListener -> eventMessageListener.onOperationMessage(record, value));
    }

    @Override
    public void onEventMessage(ConsumerRecord<String, Serializable> record, PublishedEventWrapper value) {
        applyForEach(eventMessageListener -> eventMessageListener.onEventMessage(record, value));
    }

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        applyForEach(eventMessageListener -> eventMessageListener.onPartitionsRevoked(partitions));
    }

    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        applyForEach(eventMessageListener -> eventMessageListener.onPartitionsAssigned(partitions));
    }

    @Override
    public void registerSeekCallback(ConsumerSeekCallback callback) {
        applyForEach(eventMessageListener -> eventMessageListener.registerSeekCallback(callback));
    }

    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        applyForEach(eventMessageListener -> eventMessageListener.onPartitionsAssigned(assignments, callback));
    }

    @Override
    public void onIdleContainer(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        applyForEach(eventMessageListener -> eventMessageListener.onIdleContainer(assignments, callback));

    }
}
