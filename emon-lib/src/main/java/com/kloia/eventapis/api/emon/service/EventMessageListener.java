package com.kloia.eventapis.api.emon.service;

import com.kloia.eventapis.kafka.PublishedEventWrapper;
import com.kloia.eventapis.pojos.Operation;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.MessageListener;

import java.io.Serializable;

public interface EventMessageListener extends MessageListener<String, Serializable> {
    @Override
    default void onMessage(ConsumerRecord<String, Serializable> data) {
        if (data.value() instanceof PublishedEventWrapper)
            onEventMessage(data, (PublishedEventWrapper) data.value());
        else if (data.value() instanceof Operation)
            onOperationMessage(data, (Operation) data.value());
    }

    void onOperationMessage(ConsumerRecord<String, Serializable> record, Operation operation);

    void onEventMessage(ConsumerRecord<String, Serializable> record, PublishedEventWrapper eventWrapper);
}
