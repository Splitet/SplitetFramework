package com.kloia.eventapis.api.emon.configuration;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.InterfacesConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import com.hazelcast.spring.context.SpringManagedContext;
import com.kloia.eventapis.api.emon.configuration.hazelcast.MulticastConfig;
import com.kloia.eventapis.api.emon.configuration.hazelcast.UserCodeDeploymentConfig;
import com.kloia.eventapis.api.emon.domain.Topic;
import com.kloia.eventapis.api.emon.domain.Topology;
import com.kloia.eventapis.api.emon.service.OperationExpirationListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import javax.annotation.PreDestroy;
import java.util.List;

@Configuration
@Import({MulticastConfig.class, InterfacesConfig.class, UserCodeDeploymentConfig.class})
@Slf4j
public class InMemoryComponents {


    @Value("${emon.hazelcast.group.name:'emon'}")
    private String hazelcastGrid;
    @Value("${emon.hazelcast.group.password:'emon123'}")
    private String hazelcastPassword;
    @Value("${emon.hazelcast.evict.freeHeapPercentage:20}")
    private Integer evictFreePercentage;

    @Value("${eventapis.eventBus.consumer.groupId}")
//    @Value("${info.build.artifact}")
    private String artifactId;
    private HazelcastInstance hazelcastInstance;

    @Autowired
    private List<HazelcastConfigurer> hazelcastConfigurers;

    @Bean
    public Config config() {
        Config config = new Config();
        GroupConfig groupConfig = config.getGroupConfig();
        groupConfig.setName(hazelcastGrid);
        groupConfig.setPassword(hazelcastPassword);
        config.setGroupConfig(groupConfig);
        config.setInstanceName(artifactId);

        for (HazelcastConfigurer hazelcastConfigurer : hazelcastConfigurers) {
            config = hazelcastConfigurer.configure(config);
        }
        return config;
    }

    @Bean
    public SpringManagedContext managedContext() {
        return new SpringManagedContext();
    }

    @Bean
    @Primary
    public HazelcastInstance hazelcastInstance(Config config, SpringManagedContext springManagedContext) {
        config.setManagedContext(springManagedContext);
        hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        return this.hazelcastInstance;
    }

    @Bean
    public OperationExpirationListener operationExpirationListener(@Autowired @Qualifier("operationsHistoryMap") IMap<String, Topology> operationsHistoryMap,
                                                                   @Autowired @Qualifier("topicsMap") IMap<String, Topic> topicsMap,
                                                                   @Autowired @Qualifier("operationsTopic") ITopic<Topology> operationsTopic) {
        return new OperationExpirationListener(operationsHistoryMap, topicsMap, operationsTopic);
    }

    @PreDestroy
    public void destroy() {
        hazelcastInstance.shutdown();
    }
}
