package com.kloia.eventapis.api.emon.configuration;

import com.kloia.eventapis.api.emon.service.TopologyService;
import com.kloia.eventapis.kafka.JsonDeserializer;
import com.kloia.eventapis.kafka.PublishedEventWrapper;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.spring.configuration.EventApisConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.config.ContainerProperties;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.regex.Pattern;

@Configuration
@Slf4j
public class EventListenConfiguration implements InitializingBean {

    @Autowired
    private EventApisConfiguration eventApisConfiguration;

    @Autowired
    private TopologyService topologyService;

    @Autowired
    private Pattern eventTopicRegex;


    private ConcurrentMessageListenerContainer<String, PublishedEventWrapper> messageListenerContainer;
    private ConcurrentMessageListenerContainer<String, Operation> operationListenerContainer;

    @Override
    public void afterPropertiesSet() {
        startEvents();
        startOperations();
    }

    private void startOperations() {
        Map<String, Object> consumerProperties = eventApisConfiguration.getEventBus().buildConsumerProperties();

        DefaultKafkaConsumerFactory<String, Operation> operationConsumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProperties, new StringDeserializer(), new JsonDeserializer<>(Operation.class));

        ContainerProperties operationContainerProperties = new ContainerProperties(Operation.OPERATION_EVENTS);
        operationContainerProperties.setMessageListener(topologyService);
        operationListenerContainer = new ConcurrentMessageListenerContainer<>(operationConsumerFactory,
                operationContainerProperties);
        operationListenerContainer.setBeanName("emon-operations");
        operationListenerContainer.start();
    }

    private void startEvents() {
        Map<String, Object> consumerProperties = eventApisConfiguration.getEventBus().buildConsumerProperties();

        DefaultKafkaConsumerFactory<String, PublishedEventWrapper> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProperties, new StringDeserializer(), new JsonDeserializer<>(PublishedEventWrapper.class));

        ContainerProperties containerProperties = new ContainerProperties(eventTopicRegex);
        containerProperties.setMessageListener(topologyService);
        messageListenerContainer = new ConcurrentMessageListenerContainer<>(consumerFactory,
                containerProperties);
        messageListenerContainer.setBeanName("emon-events");
        messageListenerContainer.start();
    }

    @PreDestroy
    public void stopListen() {
        if (messageListenerContainer != null && messageListenerContainer.isRunning())
            messageListenerContainer.stop();
    }

}
