package io.splitet.core.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.splitet.core.api.IUserContext;
import io.splitet.core.common.OperationContext;
import io.splitet.core.pojos.Operation;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaOperationRepositoryFactory {
    private KafkaProperties kafkaProperties;
    private IUserContext userContext;
    private OperationContext operationContext;

    public KafkaOperationRepositoryFactory(KafkaProperties kafkaProperties, IUserContext userContext, OperationContext operationContext) {
        this.kafkaProperties = kafkaProperties;
        this.userContext = userContext;
        this.operationContext = operationContext;
    }

    public KafkaOperationRepository createKafkaOperationRepository(ObjectMapper objectMapper) {
        KafkaProducer<String, Operation> operationsKafka = new KafkaProducer<>(
                kafkaProperties.buildProducerProperties(),
                new StringSerializer(),
                new JsonSerializer<>(objectMapper)
        );
        KafkaProducer<String, PublishedEventWrapper> eventsKafka = new KafkaProducer<>(
                kafkaProperties.buildProducerProperties(),
                new StringSerializer(),
                new JsonSerializer<>(objectMapper)
        );
        return new KafkaOperationRepository(
                operationContext,
                userContext,
                operationsKafka,
                eventsKafka,
                kafkaProperties.getConsumer().getGroupId()
        );
    }
}
