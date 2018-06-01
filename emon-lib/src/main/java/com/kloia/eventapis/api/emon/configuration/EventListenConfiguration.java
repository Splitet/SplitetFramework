package com.kloia.eventapis.api.emon.configuration;

import com.kloia.eventapis.api.emon.service.EventMessageListener;
import com.kloia.eventapis.api.emon.service.MultipleEventMessageListener;
import com.kloia.eventapis.kafka.JsonDeserializer;
import com.kloia.eventapis.kafka.PublishedEventWrapper;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.spring.configuration.EventApisConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.config.ContainerProperties;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Configuration
@Slf4j
@Import(EventApisConfiguration.class)
public class EventListenConfiguration implements InitializingBean {

    @Autowired
    private EventApisConfiguration eventApisConfiguration;

    @Autowired
    private List<EventMessageListener> eventMessageListeners;

    private ConcurrentMessageListenerContainer<String, PublishedEventWrapper> messageListenerContainer;
    private ConcurrentMessageListenerContainer<String, Operation> operationListenerContainer;

    @Value(value = "${eventapis.eventBus.eventTopicRegex:^(.+Event|operation-events)$}")
    private String eventTopicRegexStr;

    @Value(value = "${eventapis.eventBus.consumerGroupRegex:^(.+-command-query|.+-command)$}")
    private String consumerGroupRegexStr;

    @Bean("eventTopicRegex")
    public Pattern eventTopicRegex() {
        return Pattern.compile(eventTopicRegexStr);
    }

    @Bean("consumerGroupRegex")
    public Pattern consumerGroupRegex() {
        return Pattern.compile(consumerGroupRegexStr);
    }

    @Override
    public void afterPropertiesSet() {
        if (eventMessageListeners != null && !eventMessageListeners.isEmpty()) {
            startEvents();
            startOperations();
        }
    }

    public boolean isRunning() {
        return messageListenerContainer.isRunning() && operationListenerContainer.isRunning();
    }

    private void startOperations() {
        Map<String, Object> consumerProperties = eventApisConfiguration.getEventBus().buildConsumerProperties();

        DefaultKafkaConsumerFactory<String, Operation> operationConsumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProperties, new StringDeserializer(), new JsonDeserializer<>(Operation.class));

        ContainerProperties operationContainerProperties = new ContainerProperties(Operation.OPERATION_EVENTS);
        operationContainerProperties.setMessageListener(new MultipleEventMessageListener(eventMessageListeners));
        operationListenerContainer = new ConcurrentMessageListenerContainer<>(operationConsumerFactory,
                operationContainerProperties);
        operationListenerContainer.setBeanName("emon-operations");
        operationListenerContainer.start();
    }

    private void startEvents() {
        Map<String, Object> consumerProperties = eventApisConfiguration.getEventBus().buildConsumerProperties();

        DefaultKafkaConsumerFactory<String, PublishedEventWrapper> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProperties, new StringDeserializer(), new JsonDeserializer<>(PublishedEventWrapper.class));

        ContainerProperties containerProperties = new ContainerProperties(Pattern.compile(eventTopicRegexStr));
        containerProperties.setMessageListener(new MultipleEventMessageListener(eventMessageListeners));
        messageListenerContainer = new ConcurrentMessageListenerContainer<>(consumerFactory,
                containerProperties);
        messageListenerContainer.setBeanName("emon-events");
        messageListenerContainer.start();
    }

    @PreDestroy
    public void stopListen() {

        if (messageListenerContainer != null && messageListenerContainer.isRunning()) {
            messageListenerContainer.stop();
        }

        if (operationListenerContainer != null && operationListenerContainer.isRunning()) {
            operationListenerContainer.stop();
        }
    }

}
