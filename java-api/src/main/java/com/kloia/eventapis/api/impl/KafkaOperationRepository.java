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
    private KafkaTemplate operationsKafka;
    private KafkaTemplate eventsKafka;

    @Autowired
    public KafkaOperationRepository(@Qualifier("operationsKafka") KafkaTemplate operationsKafka,
                                    @Qualifier("eventsKafka") KafkaTemplate eventsKafka) {
        this.eventsKafka = eventsKafka;
        this.operationsKafka = operationsKafka;
    }

    @Override
    public void createOperation(String eventName, UUID opId) {
        operationsKafka.send("operation-events",opId, new CreateOperationEvent(eventName));
    }


/*    @Override
    public void publishEvent(UUID opId, Event event) {
        kafkaTemplate.send("operations",opId, event);

    }*/

    @Override
    public void appendEvent(UUID opId, Event event) {
        operationsKafka.send("operation-events",opId, new AppendEventToOperation(event));
    }

    @Override
    public void updateEvent(UUID opId, UUID eventId, SerializableConsumer<Event> action) {
        operationsKafka.send("operation-events",opId, new UpdateOperationEvent(eventId,action));
    }

    @Override
    public void failOperation(UUID opId, UUID eventId, SerializableConsumer<Event> action) {
        operationsKafka.send("operation-events",opId, new FailOperationEvent(eventId,action));
    }

    public void publishEvent(String name, PublishedEventWrapper event) {
        eventsKafka.send(name,event.getOpId(),event); // todo improve this
    }
}
