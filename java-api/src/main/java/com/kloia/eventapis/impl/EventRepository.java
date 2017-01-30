package com.kloia.eventapis.impl;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kloia.eventapis.pojos.Aggregate;
import com.kloia.eventapis.pojos.Event;
import com.kloia.eventapis.pojos.EventContext;
import com.kloia.eventapis.pojos.IEventType;
import com.sun.deploy.security.EnhancedJarVerifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteQueue;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.CollectionConfiguration;
import org.jetbrains.annotations.NotNull;


import java.util.UUID;
import java.util.concurrent.*;

/**
 * Created by zeldalozdemir on 26/01/2017.
 */
@Slf4j
public class EventRepository {


    private Ignite ignite;

    public EventRepository(Ignite ignite) {
        this.ignite = ignite;

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
                    }catch(Exception e){
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
}
