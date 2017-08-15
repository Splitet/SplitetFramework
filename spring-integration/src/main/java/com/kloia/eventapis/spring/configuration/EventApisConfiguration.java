package com.kloia.eventapis.spring.configuration;

import com.kloia.eventapis.cassandra.CassandraSession;
import com.kloia.eventapis.cassandra.EventStoreConfig;
import com.kloia.eventapis.kafka.KafkaProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;
import java.util.Map;

@ConfigurationProperties("eventapis")
@Slf4j
@Data
public class EventApisConfiguration  {
    private EventStoreConfig storeConfig;
    private KafkaProperties eventBus;
    private Map<String, String> tableNames;

/*
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
//        beanFactory.add
    }*/

    @PostConstruct
    public void registerBeans(){
        log.info("EventApis registerBeans: " + this.toString());

    }

/*
    public void setStoreConfig(EventStoreConfig storeConfig) {
        this.storeConfig = storeConfig;
        beanFactory.registerSingleton("cassandraSession",new CassandraSession(storeConfig));
    }

    public void setTableNames(Map<String, String> tableNames) {
        this.tableNames = tableNames;
    }

    public void setEventBus(KafkaProperties eventBus) {
        this.eventBus = eventBus;
    }

    public KafkaProperties getEventBus() {
        return eventBus;
    }

    public Map<String, String> getTableNames() {
        return tableNames;
    }

    public EventStoreConfig getStoreConfig() {
        return storeConfig;
    }*/
}
