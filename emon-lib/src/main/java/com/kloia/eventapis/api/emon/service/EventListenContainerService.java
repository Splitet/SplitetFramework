package com.kloia.eventapis.api.emon.service;

import com.hazelcast.quorum.Quorum;
import com.kloia.eventapis.api.emon.configuration.hazelcast.InMemoryFailedEvent;
import com.kloia.eventapis.api.emon.configuration.hazelcast.InMemoryRestoredEvent;
import com.kloia.eventapis.kafka.PublishedEventWrapper;
import com.kloia.eventapis.pojos.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;

@Service
public class EventListenContainerService {


    private final Object listenerManagementLock = new Object();
    @Autowired
    @Qualifier("messageListenerContainer")
    private ConcurrentMessageListenerContainer<String, PublishedEventWrapper> messageListenerContainer;
    @Autowired
    @Qualifier("operationListenerContainer")
    private ConcurrentMessageListenerContainer<String, Operation> operationListenerContainer;
    @Autowired(required = false)
    private Quorum defaultQuorum;
    private boolean started = false;

    public EventListenContainerService() {
    }

    @EventListener
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        synchronized (listenerManagementLock) {
            if (defaultQuorum == null || defaultQuorum.isPresent())
                startListen();
        }
    }

    @EventListener
    public void onApplicationEvent(InMemoryFailedEvent inMemoryFailedEvent) {
        stopListen();
    }

    @EventListener
    public void onApplicationEvent(InMemoryRestoredEvent inMemoryRestoredEvent) {
        startListen();
    }


    public boolean isRunning() {
        return messageListenerContainer.isRunning() && operationListenerContainer.isRunning();
    }


    public void startListen() {
        synchronized (listenerManagementLock) {
            if (messageListenerContainer != null && !messageListenerContainer.isRunning()) {
                messageListenerContainer.start();
            }

            if (operationListenerContainer != null && !operationListenerContainer.isRunning()) {
                operationListenerContainer.start();
            }
        }
    }

    @PreDestroy
    public void stopListen() {
        synchronized (listenerManagementLock) {
            if (messageListenerContainer != null && messageListenerContainer.isRunning()) {
                messageListenerContainer.stop();
            }

            if (operationListenerContainer != null && operationListenerContainer.isRunning()) {
                operationListenerContainer.stop();
            }
        }
    }

}
