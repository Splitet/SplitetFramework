package com.kloia.eventapis;

import com.kloia.eventapis.store.pojos.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.*;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.CollectionConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.IgniteProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.annotation.PostConstruct;
import java.util.UUID;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by mali on 20/01/2017.
 */
@Slf4j
@SpringBootApplication
public class Eventapis {
    private final static String IGNITE_CONFIGURATION_FILE = "ignite.xml";

    @Autowired
    private ApplicationContext applicationContext;

    public static void main(String[] args) {
        System.setProperty(IgniteSystemProperties.IGNITE_UPDATE_NOTIFIER, String.valueOf(false));
        ConfigurableApplicationContext context = SpringApplication.run(Eventapis.class, args);
    }


    @Bean("ignite")
    @Scope("singleton")
    @Primary
    public Ignite createIgnite() throws IgniteCheckedException {
        return Ignition.start(IGNITE_CONFIGURATION_FILE);
    }

    @Autowired Ignite ignite;


    @PostConstruct
    public void start(){
        log.info("Application is started for Node:"+ ignite.cluster().nodes());
        CollectionConfiguration cfg = new CollectionConfiguration();
        IgniteQueue<Object> queue = ignite.queue("main", 0, cfg);
        IgniteCache<UUID, Transaction> transactionCache = ignite.cache("transactionCache");
        log.info("Application is started for KeySizes:"+ transactionCache.size(CachePeekMode.PRIMARY));
        transactionCache.put(UUID.randomUUID(), new Transaction("Entity","ok"));
        log.info("Application is started for KeySizes:"+ transactionCache.size(CachePeekMode.PRIMARY));
        log.info(transactionCache.get(UUID.fromString("4447a089-e5f7-477c-9807-79210fafa296")).toString());
    }
}
