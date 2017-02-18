package com.kloia.eventapis.api.store;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import com.kloia.eventapis.api.store.filter.EntityRestTemplate;
import com.kloia.eventapis.api.store.filter.ReqInterceptor;
import com.kloia.eventapis.api.pojos.Event;
import com.kloia.eventapis.api.pojos.Operation;
import com.kloia.eventapis.api.pojos.TransactionState;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.*;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.cache.store.cassandra.datasource.DataSource;
import org.apache.ignite.cache.store.cassandra.persistence.KeyValuePersistenceSettings;
import org.apache.ignite.configuration.CollectionConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ResourceLoader;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.script.ScriptEngineManager;
import java.util.ArrayList;
import java.util.UUID;

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
        return IgniteSpring.start(IGNITE_CONFIGURATION_FILE,applicationContext);
    }

    @Autowired
    private ResourceLoader resourceLoader;


    @Bean("keyValuePersistenceSettings")
    @Primary
    public KeyValuePersistenceSettings createKeyValuePersistenceSettings() {
        return new KeyValuePersistenceSettings(resourceLoader.getResource("classpath:persistence.xml"));
    }
    @Bean("cassandraAdminDataSource")
    @Primary
    public DataSource createDataSource() {
        DataSource dataSource = new DataSource();
        dataSource.setContactPoints("127.0.0.1");
        dataSource.setPort(9042);
        dataSource.setReadConsistency(ConsistencyLevel.ONE.name());
        dataSource.setWriteConsistency(ConsistencyLevel.ONE.name());
        dataSource.setLoadBalancingPolicy(new RoundRobinPolicy());
        return dataSource;
    }

    @Autowired
    Ignite ignite;


    @PostConstruct
    public void start() {
        log.info("Application is started for Node:" + ignite.cluster().nodes());
        CollectionConfiguration cfg = new CollectionConfiguration();
        IgniteQueue<Object> queue = ignite.queue("main", 0, cfg);
        IgniteCache<UUID, Operation> operationCache = ignite.cache("operationCache");
        log.info("Application is started for KeySizes:" + operationCache.size(CachePeekMode.PRIMARY));
        operationCache.put(UUID.randomUUID(), new Operation("",new ArrayList<Event>(), TransactionState.RUNNING));
        log.info("Application is started for KeySizes:" + operationCache.size(CachePeekMode.PRIMARY));
//        log.info(transactionCache.get(UUID.fromString("4447a089-e5f7-477c-9807-79210fafa296")).toString());
    }

    @Bean
    public EntityRestTemplate entityRestTemplate() {
        EntityRestTemplate entityRestTemplate = new EntityRestTemplate();
        entityRestTemplate.getInterceptors().add(new ReqInterceptor());
        return new EntityRestTemplate();

    }

}
