package io.splitet.core.api.emon.configuration;


import io.splitet.core.api.emon.service.ConsumerOffsetListener;
import io.splitet.core.spring.configuration.EventApisConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.Map;

@Configuration
@Slf4j
public class ConsumerOffsetListenerConfiguration {

    public static final String CONSUMER_OFFSETS = "__consumer_offsets";

    @Autowired
    private EventApisConfiguration eventApisConfiguration;

    @Autowired
    private ConsumerOffsetListener consumerOffsetListener;

    @Bean("consumerOffsetListenerContainer")
    public ConcurrentMessageListenerContainer<byte[], byte[]> consumerOffsetListenerContainer() {
        Map<String, Object> consumerProperties = eventApisConfiguration.getEventBus().buildConsumerProperties();
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        ContainerProperties containerProperties = new ContainerProperties(CONSUMER_OFFSETS);
        containerProperties.setMessageListener(consumerOffsetListener);
        containerProperties.setAckMode(ContainerProperties.AckMode.TIME); // To avoid echoings
        containerProperties.setAckTime(3000);

        DefaultKafkaConsumerFactory<byte[], byte[]> operationConsumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProperties, new ByteArrayDeserializer(), new ByteArrayDeserializer());

        ConcurrentMessageListenerContainer<byte[], byte[]> consumerOffsetListenerContainer =
                new ConcurrentMessageListenerContainer<>(operationConsumerFactory, containerProperties);
        consumerOffsetListenerContainer.setConcurrency(5);
        consumerOffsetListenerContainer.setBeanName("consumer-offsets");

        return consumerOffsetListenerContainer;
    }


}
