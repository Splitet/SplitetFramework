package com.kloia.eventapis.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.pojos.Operation;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaOperationRepositoryFactory {
    private KafkaProperties kafkaProperties;

    public KafkaOperationRepositoryFactory(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    public KafkaOperationRepository createKafkaOperationRepository(ObjectMapper objectMapper) {
        KafkaProducer<String, Operation> operationsKafka = new KafkaProducer<>(
                kafkaProperties.buildProducerProperties(), new StringSerializer(), new JsonSerializer<>(objectMapper));
        KafkaProducer<String, PublishedEventWrapper> eventsKafka = new KafkaProducer<>(kafkaProperties.buildProducerProperties(),
                new StringSerializer(), new JsonSerializer<>(objectMapper));
        return new KafkaOperationRepository(operationsKafka, eventsKafka, kafkaProperties.getConsumer().getGroupId());
    }

    public Consumer<String, PublishedEventWrapper> createEventConsumer(ObjectMapper objectMapper) {
        KafkaProperties properties = kafkaProperties.clone();
        properties.getConsumer().setEnableAutoCommit(true);
        return new KafkaConsumer<>(properties.buildConsumerProperties(),
                new StringDeserializer(), new JsonDeserializer<>(PublishedEventWrapper.class,objectMapper));
    }
    public Consumer<String, Operation> createOperationConsumer(ObjectMapper objectMapper) {
        KafkaProperties properties = kafkaProperties.clone();
        properties.getConsumer().setEnableAutoCommit(false);
        return new KafkaConsumer<>(properties.buildConsumerProperties(),
                new StringDeserializer(), new JsonDeserializer<>(Operation.class,objectMapper));
    }
/*
    public boolean isAutoCommit() {
        return Boolean.TRUE.equals(kafkaProperties.getConsumer().getEnableAutoCommit());
    }*/
}
