package com.kloia.eventapis.api.impl;

import com.kloia.eventapis.pojos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by zeldalozdemir on 20/04/2017.
 */
@Service
public class KafkaOperationRepository implements IOperationRepository {
    private KafkaTemplate<UUID,Operation> operationsKafka;
    private KafkaTemplate<UUID,PublishedEventWrapper> eventsKafka;

    @Autowired
    public KafkaOperationRepository(@Qualifier("operationsKafka") KafkaTemplate<UUID,Operation> operationsKafka,
                                    @Qualifier("eventsKafka") KafkaTemplate<UUID,PublishedEventWrapper> eventsKafka) {
        this.eventsKafka = eventsKafka;
        this.operationsKafka = operationsKafka;
    }

    @Override
    public void createOperation(String eventName, UUID opId) {
//        operationsKafka.send("operation-events",opId, new CreateOperationEvent(eventName));
    }


/*    @Override
    public void publishEvent(UUID opId, Event event) {
        kafkaTemplate.send("operations",opId, event);

    }*/

    @Override
    public void appendEvent(UUID opId, Event event) {
//        operationsKafka.send("operation-events",opId, new AppendEventToOperation(event));
    }

    @Override
    public void updateEvent(UUID opId, UUID eventId, SerializableConsumer<Event> action) {
//        operationsKafka.send("operation-events",opId, new UpdateOperationEvent(eventId,action));
    }

    @Override
    public void failOperation(UUID opId, UUID eventId, SerializableConsumer<Event> action) {
        Operation operation = new Operation();
        operation.setTransactionState(TransactionState.TXN_FAILED);
        operationsKafka.send("operation-events",opId, operation);
    }

    public void publishEvent(String name, PublishedEventWrapper event) {
        eventsKafka.send(name,event.getOpId(),event); // todo improve this
    }
}
