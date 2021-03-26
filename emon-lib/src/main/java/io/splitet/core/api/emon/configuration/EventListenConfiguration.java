package io.splitet.core.api.emon.configuration;

import io.splitet.core.api.emon.service.EventMessageListener;
import io.splitet.core.api.emon.service.MultipleEventMessageListener;
import io.splitet.core.kafka.JsonDeserializer;
import io.splitet.core.kafka.PublishedEventWrapper;
import io.splitet.core.pojos.Operation;
import io.splitet.core.spring.configuration.EventApisConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Configuration
@Slf4j
@Import(EventApisConfiguration.class)
public class EventListenConfiguration {

    @Autowired
    private EventApisConfiguration eventApisConfiguration;

    @Autowired
    private List<EventMessageListener> eventMessageListeners;

    @Value(value = "${eventapis.eventBus.eventTopicRegex:^.+Event$}")
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

    @Bean(name = "operationListenerContainer")
    public ConcurrentMessageListenerContainer<String, Operation> operationListenerContainer() {
        Map<String, Object> consumerProperties = eventApisConfiguration.getEventBus().buildConsumerProperties();
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        DefaultKafkaConsumerFactory<String, Operation> operationConsumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProperties, new StringDeserializer(), new JsonDeserializer<>(Operation.class));

        ContainerProperties containerProperties = new ContainerProperties(Operation.OPERATION_EVENTS);
        containerProperties.setMessageListener(new MultipleEventMessageListener(eventMessageListeners));
        containerProperties.setAckMode(ContainerProperties.AckMode.BATCH);
        ConcurrentMessageListenerContainer<String, Operation> operationListenerContainer = new ConcurrentMessageListenerContainer<>(operationConsumerFactory, containerProperties);
        operationListenerContainer.setBeanName("emon-operations");
        operationListenerContainer.setConcurrency(eventApisConfiguration.getEventBus().getConsumer().getOperationConcurrency());
        return operationListenerContainer;
    }

    @Bean(name = "messageListenerContainer")
    public ConcurrentMessageListenerContainer<String, PublishedEventWrapper> messageListenerContainer() {
        Map<String, Object> consumerProperties = eventApisConfiguration.getEventBus().buildConsumerProperties();
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        consumerProperties.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, 3000);

        DefaultKafkaConsumerFactory<String, PublishedEventWrapper> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProperties, new StringDeserializer(), new JsonDeserializer<>(PublishedEventWrapper.class));

        ContainerProperties containerProperties = new ContainerProperties(Pattern.compile(eventTopicRegexStr));
        containerProperties.setMessageListener(new MultipleEventMessageListener(eventMessageListeners));
        containerProperties.setAckMode(ContainerProperties.AckMode.BATCH);
        ConcurrentMessageListenerContainer<String, PublishedEventWrapper> messageListenerContainer = new ConcurrentMessageListenerContainer<>(consumerFactory, containerProperties);
        messageListenerContainer.setConcurrency(eventApisConfiguration.getEventBus().getConsumer().getEventConcurrency());
        messageListenerContainer.setBeanName("emon-events");
        return messageListenerContainer;
    }

}
