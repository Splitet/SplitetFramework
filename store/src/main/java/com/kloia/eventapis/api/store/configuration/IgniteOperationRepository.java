package com.kloia.eventapis.api.store.configuration;

import com.kloia.eventapis.api.impl.IOperationRepository;
import com.kloia.eventapis.api.impl.SerializableConsumer;
import com.kloia.eventapis.pojos.Event;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.pojos.PublishedEventWrapper;
import com.kloia.eventapis.pojos.TransactionState;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheEntryProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 26/01/2017.
 */
@Slf4j
@Component
public class IgniteOperationRepository implements IOperationRepository {


    private Ignite ignite;
    private final IgniteCache<UUID, Operation> operationCache;
    private KafkaTemplate kafkaTemplate;


    public static String topic = "operation-events";

    @Autowired
    public IgniteOperationRepository(@Qualifier("operationIgniteClient") Ignite ignite, KafkaTemplate kafkaTemplate) {
        this.ignite = ignite;
        operationCache = ignite.cache("operationCache");

        this.kafkaTemplate = kafkaTemplate;
    }



//    @Override        kafkaTemplate.send("operations",opId, new UpdateEventOfOperation(eventId,action));

    public void createOperation(String mainAggregateName, UUID opId) {
        Operation operation = new Operation(mainAggregateName, new ArrayList<>(), TransactionState.RUNNING);
        UUID opid = UUID.randomUUID();
        operationCache.putIfAbsent(opid, operation);
        kafkaTemplate.send(topic,opid,operation);
    }

    @Override
    public void publishEvent(String name, PublishedEventWrapper event) {
        kafkaTemplate.send(name,event);
    }

    @Override
    public void appendEvent(UUID opId, Event event) {
        Operation result = operationCache.invoke(opId, (entry, arguments) -> {
            entry.getValue().getEvents().add((Event) arguments[0]);
            return entry.getValue();
        }, event);
        kafkaTemplate.send(topic,opId,result);
    }

    @Override
    public void updateEvent(UUID opId, UUID eventId, SerializableConsumer<Event> action) {
//        new ArrayList<Event>().forEach();
        operationCache.invoke(opId, (CacheEntryProcessor<UUID, Operation, Operation>) (entry, arguments) -> {
            UUID eventIdArg = (UUID) arguments[0];
            SerializableConsumer<Event> actionArg = (SerializableConsumer<Event>) arguments[1];
            Operation operation = entry.getValue();
            Optional<Event> first = operation.getEventFor(eventIdArg);
            first.ifPresent(actionArg::accept);
            return operation;
        }, eventId, action);
    }

    @Override
    public void failOperation(UUID opId, UUID eventId, SerializableConsumer<Event> action) {
//        new ArrayList<Event>().forEach();
        Operation result = operationCache.invoke(opId, (CacheEntryProcessor<UUID, Operation, Operation>) (entry, arguments) -> {
            UUID eventIdArg = (UUID) arguments[0];
            SerializableConsumer<Event> actionArg = (SerializableConsumer<Event>) arguments[1];
            Operation operation = entry.getValue();
            Optional<Event> first = operation.getEventFor(eventIdArg);
            first.ifPresent(actionArg::accept);
            operation.setTransactionState(TransactionState.TXN_FAILED);
            return operation;
        }, eventId, action);
        kafkaTemplate.send(topic,opId,result);
    }
}
