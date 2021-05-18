package com.kloia.eventapis.api.emon.service;

import com.hazelcast.quorum.Quorum;
import com.kloia.eventapis.api.emon.configuration.hazelcast.InMemoryFailedEvent;
import com.kloia.eventapis.api.emon.configuration.hazelcast.InMemoryRestoredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;

@Service
@Slf4j
public class ConsumerOffsetListenerContainerService {

    @Autowired
    @Qualifier("consumerOffsetListenerContainer")
    private ConcurrentMessageListenerContainer<byte[], byte[]> consumerOffsetListenerContainer;

    @Autowired(required = false)
    private Quorum defaultQuorum;

    private final Object listenerManagementLock = new Object();


    @org.springframework.context.event.EventListener
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        synchronized (listenerManagementLock) {
            if (defaultQuorum == null || defaultQuorum.isPresent()) {
                startListen();
            }
        }
    }

    @org.springframework.context.event.EventListener
    public void onApplicationEvent(InMemoryFailedEvent inMemoryFailedEvent) {
        stopListen();
    }

    @org.springframework.context.event.EventListener
    public void onApplicationEvent(InMemoryRestoredEvent inMemoryRestoredEvent) {
        startListen();
    }

    public void startListen() {
        synchronized (listenerManagementLock) {
            if (consumerOffsetListenerContainer != null && !consumerOffsetListenerContainer.isRunning()) {
                consumerOffsetListenerContainer.start();
            }
        }
    }

    @PreDestroy
    public void stopListen() {
        synchronized (listenerManagementLock) {
            if (consumerOffsetListenerContainer != null && consumerOffsetListenerContainer.isRunning()) {
                consumerOffsetListenerContainer.stop();
            }
        }
    }

}
