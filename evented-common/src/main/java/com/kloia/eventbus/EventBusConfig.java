package com.kloia.eventbus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.pojos.PublishedEventWrapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 26/02/2017.
 */
@Configuration
@EnableKafka
@PropertySources({
        @PropertySource("classpath:application.yml"),
        @PropertySource("classpath:bootstrap.yml")
})
public class EventBusConfig {

    @Value("${eventbus.servers}")
    private String kafkaServerAddresses;


    @Value("${info.build.artifact}")
    private String artifactId;


    public Map producerConfigs() {
        Map props = new HashMap<>();
        // list of host:port pairs used for establishing the initial connections
        // to the Kakfa cluster
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafkaServerAddresses);
//        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
//                JsonSerializer.class);
//        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
//                JsonSerializer.class);
        // value to block, after which it will throw a TimeoutException
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5000);

        return props;
    }

    @Bean
    @Qualifier("operationsKafkaProducer")
    public ProducerFactory<UUID, Operation> operationProducerFactory(@Autowired ObjectMapper objectMapper) {
        return new DefaultKafkaProducerFactory<UUID, Operation>(producerConfigs(), new JsonSerializer(objectMapper), new JsonSerializer<>(objectMapper));
    }

    @Bean
    @Qualifier("operationsKafka")
    public KafkaTemplate<UUID, Operation> operationsKafkaTemplate(@Qualifier("operationsKafkaProducer") ProducerFactory<UUID, Operation> operationProducerFactory) {
        return new KafkaTemplate<UUID, Operation>(operationProducerFactory);
    }

    @Bean
    @Qualifier("eventsKafkaProducer")
    public ProducerFactory<UUID, PublishedEventWrapper> eventProducerFactory(@Autowired ObjectMapper objectMapper) {
        return new DefaultKafkaProducerFactory<UUID, PublishedEventWrapper>(producerConfigs(), new JsonSerializer(objectMapper), new JsonSerializer<>(objectMapper));
    }

    @Bean
    @Qualifier("eventsKafka")
    public KafkaTemplate<UUID, PublishedEventWrapper> eventsKafkaTemplate(@Qualifier("eventsKafkaProducer") ProducerFactory<UUID, PublishedEventWrapper> eventProducerFactory) {
        return new KafkaTemplate<UUID, PublishedEventWrapper>(eventProducerFactory);
    }


    public Map consumerConfigs() {
        Map props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafkaServerAddresses);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, artifactId);
//        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "operations-client");
//        props.put(ConsumerConfig., "operations");

        return props;
    }

    @Bean("operationsConsumerFactory")
//    @Qualifier("operationsConsumerFactory")
    public ConsumerFactory<UUID, Operation> operationsConsumerFactory(@Autowired ObjectMapper objectMapper) {
        return new DefaultKafkaConsumerFactory<UUID, Operation>(consumerConfigs(), new JsonDeserializer<UUID>(UUID.class), new JsonDeserializer(Operation.class, objectMapper));
    }

    @Bean({"eventsConsumerFactory"})
//    @Qualifier("consumerFactory")
    public ConsumerFactory<UUID, PublishedEventWrapper> eventsConsumerFactory(@Autowired ObjectMapper objectMapper) {
        DefaultKafkaConsumerFactory<UUID, PublishedEventWrapper> eventsConsumerFactory = new DefaultKafkaConsumerFactory<UUID, PublishedEventWrapper>(consumerConfigs(), new JsonDeserializer<UUID>(UUID.class), new JsonDeserializer<PublishedEventWrapper>(PublishedEventWrapper.class, objectMapper));
        return eventsConsumerFactory;
    }

    @Bean("operationsKafkaListenerContainerFactory")
//    @Qualifier("operationsKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<UUID, Operation> operationsKafkaListenerContainerFactory(@Qualifier("operationsConsumerFactory") ConsumerFactory<UUID, Operation> operationsConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<UUID, Operation> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(operationsConsumerFactory);
        factory.setConcurrency(3);
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }

    @Bean({"eventsKafkaListenerContainerFactory", "kafkaListenerContainerFactory"})
//    @Qualifier("eventsKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<UUID, PublishedEventWrapper> eventsKafkaListenerContainerFactory(
            @Qualifier("eventsConsumerFactory") ConsumerFactory<UUID, PublishedEventWrapper> eventsConsumerFactory,
            ObjectMapper objectMapper, EventMessageConverter eventMessageConverter) {
        ConcurrentKafkaListenerContainerFactory<UUID, PublishedEventWrapper> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(eventsConsumerFactory);
        factory.setConcurrency(3);
        factory.setMessageConverter(eventMessageConverter);
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }


}
