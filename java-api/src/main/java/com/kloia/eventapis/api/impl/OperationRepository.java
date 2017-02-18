package com.kloia.eventapis.api.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kloia.eventapis.api.pojos.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteQueue;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.CollectionConfiguration;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Created by zeldalozdemir on 26/01/2017.
 */
@Slf4j
public class OperationRepository {


    private Ignite ignite;
    private final IgniteCache<UUID, Operation> operationCache;

    ThreadLocal<AbstractMap.SimpleEntry<UUID,Operation>> operationContext = new ThreadLocal<AbstractMap.SimpleEntry<UUID,Operation>>(){
        @Override
        protected AbstractMap.SimpleEntry<UUID, Operation> initialValue() {
            return super.initialValue();
        }
    };

    public OperationRepository(Ignite ignite) {
        this.ignite = ignite;
        operationCache = ignite.cache("operationCache");

    }

    public void registerForEvent(Aggregate aggregate, String... events) {

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
                        EventContext.setEventContext(new EventContext(poll.getTransactionId()));
                        Event result = aggregate.handleEvent(poll);
                        queue.offer(result);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        EventContext.setEventContext(null);
                    }
            }, 1, 1, TimeUnit.SECONDS);
        }
    }

    public void sendEvent(String eventName, String[] params) {
        IgniteQueue<Event> queue = ignite.queue(eventName, 100000, null);
        Event event = EventContext.createNewEvent(IEventType.EXECUTE, params);
        queue.offer(event);
    }

    public Map.Entry<UUID, Operation> createOperation(String mainAggregateName) {
        Operation operation = new Operation(mainAggregateName, new ArrayList<>(), TransactionState.RUNNING);
        UUID opid = UUID.randomUUID();
        operationCache.putIfAbsent(opid, operation);
        return new AbstractMap.SimpleEntry<UUID, Operation>(opid, operation);
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
}
