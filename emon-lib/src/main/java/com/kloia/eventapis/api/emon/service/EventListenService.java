package com.kloia.eventapis.api.emon.service;

import com.kloia.eventapis.api.emon.configuration.StoreConfiguration;
import com.kloia.eventapis.kafka.JsonDeserializer;
import com.kloia.eventapis.kafka.PublishedEventWrapper;
import com.kloia.eventapis.pojos.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class EventListenService {

    @Autowired
    private StoreConfiguration storeConfiguration;
    @Autowired
    private TopicService topicService;
    @Autowired
    private TopologyService topologyService;

    private ConcurrentMessageListenerContainer<String, PublishedEventWrapper> messageListenerContainer;
    private ConcurrentMessageListenerContainer<String, Operation> operationListenerContainer;


    @PostConstruct
    public void startListen() throws ExecutionException, InterruptedException {
        startEvents();
        startOperations();
    }

    private void startOperations() {
        Map<String, Object> consumerProperties = storeConfiguration.getEventBus().buildConsumerProperties();

        DefaultKafkaConsumerFactory<String, Operation> operationConsumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProperties, new StringDeserializer(), new JsonDeserializer<>(Operation.class));

        ContainerProperties operationContainerProperties = new ContainerProperties(Operation.OPERATION_EVENTS);
        operationContainerProperties.setMessageListener(topologyService);
        operationListenerContainer = new ConcurrentMessageListenerContainer<>(operationConsumerFactory,
                operationContainerProperties);
        operationListenerContainer.setBeanName("OpStore-Operations");
        operationListenerContainer.start();
    }

    private void startEvents() throws ExecutionException, InterruptedException {
        Map<String, Object> consumerProperties = storeConfiguration.getEventBus().buildConsumerProperties();

        DefaultKafkaConsumerFactory<String, PublishedEventWrapper> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProperties, new StringDeserializer(), new JsonDeserializer<>(PublishedEventWrapper.class));

        String[] topics = topicService.queryTopicListAsArr();
        log.info("Starting to Listen Events:" + String.join(",", topics));
        ContainerProperties containerProperties = new ContainerProperties(topics);
        containerProperties.setMessageListener(topologyService);
        messageListenerContainer = new ConcurrentMessageListenerContainer<>(consumerFactory,
                containerProperties);
        messageListenerContainer.setBeanName("OpStore-Events");
        messageListenerContainer.start();
    }

    @PreDestroy
    public void stopListen() {
        if (messageListenerContainer != null && messageListenerContainer.isRunning())
            messageListenerContainer.stop();
    }
}
