package com.kloia.eventapis.api.impl;

import com.kloia.eventapis.pojos.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheEntryProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
<<<<<<< HEAD
import org.springframework.kafka.core.KafkaTemplate;
=======
>>>>>>> fe1c6bd...  - Evented DB usage examples
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by zeldalozdemir on 26/01/2017.
 */
@Slf4j
@Component
public class OperationRepository {


    private Ignite ignite;
    private final IgniteCache<UUID, Operation> operationCache;
    private KafkaTemplate kafkaTemplate;

    ThreadLocal<AbstractMap.SimpleEntry<UUID,Operation>> operationContext = new ThreadLocal<AbstractMap.SimpleEntry<UUID,Operation>>(){
        @Override
        protected AbstractMap.SimpleEntry<UUID, Operation> initialValue() {
            return super.initialValue();
        }
    };
    public static String topic = "operation-events";

    @Autowired
<<<<<<< HEAD
    public OperationRepository(@Qualifier("operationIgniteClient") Ignite ignite, KafkaTemplate kafkaTemplate) {
=======
    public OperationRepository(@Qualifier("operationIgniteClient") Ignite ignite) {
>>>>>>> fe1c6bd...  - Evented DB usage examples
        this.ignite = ignite;
        operationCache = ignite.cache("operationCache");

        this.kafkaTemplate = kafkaTemplate;
    }

/*    public void registerForEvent(Aggregate aggregate, String... events) {

        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("my-sad-thread-%d").build();

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(events.length, threadFactory);

        for (String event : events) {
            CollectionConfiguration cfg = new CollectionConfiguration();
            cfg.setAtomicityMode(CacheAtomicityMode.ATOMIC);
            IgniteQueue<Event> queue = ignite.queue(event, 1000000, cfg);
            executorService.scheduleWithFixedDelay(() -> {
                Event poll = queue.poll(3, TimeUnit.SECONDS);
                if (poll != null)
                    try {
                        System.out.println("Polled:" + poll);
                        EventContext.setEventContext(new EventContext(poll.get()));
                        Event result = aggregate.handleEvent(poll);
                        queue.offer(result);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        EventContext.setEventContext(null);
                    }
            }, 1, 1, TimeUnit.SECONDS);
        }
    }*/

/*    public void sendEvent(String eventName, String[] params) {
        IgniteQueue<Event> queue = ignite.queue(eventName, 100000, null);
        Event event = EventContext.createNewEvent(IEventType.EXECUTE, params);
        queue.offer(event);
    }*/

    public Map.Entry<UUID, Operation> createOperation(String mainAggregateName) {
        Operation operation = new Operation(mainAggregateName, new ArrayList<>(), TransactionState.RUNNING);
        UUID opid = UUID.randomUUID();
        operationCache.putIfAbsent(opid, operation);
        AbstractMap.SimpleEntry<UUID, Operation> uuidOperationSimpleEntry = new AbstractMap.SimpleEntry<>(opid, operation);
        kafkaTemplate.send(topic,opid,operation);
        return uuidOperationSimpleEntry;
    }

    public Operation getOperation(UUID opid) {
        return operationCache.get(opid);
    }
    public void switchContext(UUID opid, Operation operation){
        operationContext.set(new AbstractMap.SimpleEntry<UUID, Operation>(opid,operation));
    }

    public Map.Entry<UUID, Operation>  getContext(){
        return operationContext.get();
    }

    public void publishEvent(Event event) {


    }

    public void clearContext() {
        operationContext.remove();
    }

    public void appendEvent(UUID opId, Event event) {
        Operation result = operationCache.invoke(opId, (entry, arguments) -> {
            entry.getValue().getEvents().add((Event) arguments[0]);
            return entry.getValue();
        }, event);
        kafkaTemplate.send(topic,opId,result);
    }

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
