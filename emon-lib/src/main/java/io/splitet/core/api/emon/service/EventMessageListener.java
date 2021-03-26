package io.splitet.core.api.emon.service;

import io.splitet.core.kafka.PublishedEventWrapper;
import io.splitet.core.pojos.Operation;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.kafka.listener.MessageListener;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public interface EventMessageListener extends MessageListener<String, Serializable>, ConsumerRebalanceListener, ConsumerSeekAware {
    @Override
    default void onMessage(ConsumerRecord<String, Serializable> data) {
        if (data.value() instanceof PublishedEventWrapper)
            onEventMessage(data, (PublishedEventWrapper) data.value());
        else if (data.value() instanceof Operation)
            onOperationMessage(data, (Operation) data.value());
    }

    void onOperationMessage(ConsumerRecord<String, Serializable> record, Operation operation);

    void onEventMessage(ConsumerRecord<String, Serializable> record, PublishedEventWrapper eventWrapper);

    @Override
    default void onPartitionsRevoked(Collection<TopicPartition> partitions) {
    }

    @Override
    default void onPartitionsAssigned(Collection<TopicPartition> partitions) {
    }

    @Override
    default void registerSeekCallback(ConsumerSeekCallback callback) {
    }

    @Override
    default void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
    }

    @Override
    default void onIdleContainer(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
    }
}
