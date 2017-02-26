package com.kloia.eventapis.api.store.configuration;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import com.kloia.eventapis.api.store.filter.EntityRestTemplate;
import com.kloia.eventapis.api.store.filter.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.*;
import org.apache.ignite.cache.store.cassandra.datasource.DataSource;
import org.apache.ignite.cache.store.cassandra.persistence.KeyValuePersistenceSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ResourceLoader;

/**
 * Created by mali on 20/01/2017.
 */
@Slf4j
@Configuration
public class StoreConfiguration {
    private final static String IGNITE_CONFIGURATION_FILE = "ignite.xml";

    @Autowired
    private ApplicationContext applicationContext;

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
    @Bean("cassandraDataSource")
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

    @Bean
    public EntityRestTemplate entityRestTemplate() {
        EntityRestTemplate entityRestTemplate = new EntityRestTemplate();
        entityRestTemplate.getInterceptors().add(new RequestInterceptor());
        return new EntityRestTemplate();

    }

}
