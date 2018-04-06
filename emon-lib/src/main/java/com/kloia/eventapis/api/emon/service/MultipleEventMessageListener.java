package com.kloia.eventapis.api.emon.service;

import com.kloia.eventapis.kafka.PublishedEventWrapper;
import com.kloia.eventapis.pojos.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.Serializable;
import java.util.List;

@Slf4j
public class MultipleEventMessageListener implements EventMessageListener {
    private final List<EventMessageListener> eventMessageListeners;

    public MultipleEventMessageListener(List<EventMessageListener> eventMessageListeners) {
        this.eventMessageListeners = eventMessageListeners;
    }

    @Override
    public void onOperationMessage(ConsumerRecord<String, Serializable> record, Operation value) {
        eventMessageListeners.forEach(eventMessageListener -> {
            try {
                eventMessageListener.onOperationMessage(record, value);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    @Override
    public void onEventMessage(ConsumerRecord<String, Serializable> record, PublishedEventWrapper value) {
        eventMessageListeners.forEach(eventMessageListener -> {
            try {
                eventMessageListener.onEventMessage(record, value);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }
}
