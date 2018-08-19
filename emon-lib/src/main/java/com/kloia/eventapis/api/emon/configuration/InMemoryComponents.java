package com.kloia.eventapis.api.emon.configuration;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapIndexConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.config.ReplicatedMapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.spring.context.SpringManagedContext;
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
import java.util.Arrays;
import java.util.List;

@Configuration
@Import({InMemoryConfig.class, InMemoryInterfacesConfig.class, InMemoryUserCodeDeploymentConfig.class})
@Slf4j
public class InMemoryComponents {

    public static final int OPERATIONS_MAX_TTL_INSEC = 60000;
    public static final String OPERATIONS_MAP_NAME = "operations";
    public static final String OPERATIONS_MAP_HISTORY_NAME = "operations-history";
    public static final String META_MAP_NAME = "meta";
    public static final String TOPICS_MAP_NAME = "topics";

    @Value("${emon.hazelcast.group.name:'emon'}")
    private String hazelcastGrid;
    @Value("${emon.hazelcast.group.password:'emon123'}")
    private String hazelcastPassword;
    @Value("${emon.hazelcast.evict.freeHeapPercentage:20}")
    private Integer evictFreePercentage;
    @Autowired(required = false)
    private InMemoryConfig inMemoryConfig;
    @Autowired(required = false)
    private InMemoryInterfacesConfig inMemoryInterfacesConfig;
    @Autowired(required = false)
    private InMemoryUserCodeDeploymentConfig inMemoryUserCodeDeploymentConfig;
    @Value("${eventapis.eventBus.consumer.groupId}")
//    @Value("${info.build.artifact}")
    private String artifactId;
    private HazelcastInstance hazelcastInstance;

    @Bean
    public Config config() {
        Config config = new Config();

        List<MapIndexConfig> indexes = Arrays.asList(
                new MapIndexConfig("startTime", true),
                new MapIndexConfig("operationState", true)
        );

        config.addMapConfig(new MapConfig()
                .setTimeToLiveSeconds(OPERATIONS_MAX_TTL_INSEC)
                .setMapIndexConfigs(indexes)
                .setName(OPERATIONS_MAP_NAME)
        );

        config.addMapConfig(new MapConfig()
                .setMapIndexConfigs(indexes)
                .setMaxSizeConfig(new MaxSizeConfig(evictFreePercentage, MaxSizeConfig.MaxSizePolicy.FREE_HEAP_PERCENTAGE))
                .setEvictionPolicy(EvictionPolicy.LRU)
                .setName(OPERATIONS_MAP_HISTORY_NAME)
        );
        config.addReplicatedMapConfig(new ReplicatedMapConfig()
                .setName(TOPICS_MAP_NAME)
        );
        /*
        config.setExecutorConfigs(Collections.singletonMap("default",new ExecutorConfig("default",2)));
        config.setProperty("hazelcast.event.thread.count","2");
        config.setProperty("hazelcast.operation.generic.thread.count","2");
        config.setProperty("hazelcast.operation.thread.count","2");
        config.setProperty("hazelcast.io.thread.count","1");
        config.setProperty("hazelcast.logging.type", "slf4j");
        */
        GroupConfig groupConfig = config.getGroupConfig();
        groupConfig.setName(hazelcastGrid);
        groupConfig.setPassword(hazelcastPassword);
        if (inMemoryConfig != null) {
            config.getNetworkConfig().getJoin().setMulticastConfig(inMemoryConfig);
        }
        if (inMemoryInterfacesConfig != null) {
            config.getNetworkConfig().setInterfaces(inMemoryInterfacesConfig);
        }
        if (inMemoryUserCodeDeploymentConfig != null)
            config.setUserCodeDeploymentConfig(inMemoryUserCodeDeploymentConfig);

        config.setGroupConfig(groupConfig);
        config.setInstanceName(artifactId);
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
    public IMap<String, Topology> operationsHistoryMap(@Autowired @Qualifier("hazelcastInstance") HazelcastInstance hazelcastInstance) {
        return hazelcastInstance.getMap(OPERATIONS_MAP_HISTORY_NAME);
    }

    @Bean
    public IMap<String, Object> metaMap(@Autowired @Qualifier("hazelcastInstance") HazelcastInstance hazelcastInstance) {
        return hazelcastInstance.getMap(META_MAP_NAME);
    }

    @Bean
    public OperationExpirationListener operationExpirationListener(@Autowired @Qualifier("operationsHistoryMap") IMap<String, Topology> operationsHistoryMap,
                                                                   @Autowired @Qualifier("topicsMap") IMap<String, Topic> topicsMap) {
        return new OperationExpirationListener(operationsHistoryMap, topicsMap);
    }

    @Bean
    public IMap<String, Topology> operationsMap(@Autowired @Qualifier("hazelcastInstance") HazelcastInstance hazelcastInstance, OperationExpirationListener operationExpirationListener) {
        IMap<String, Topology> operationsMap = hazelcastInstance.getMap(OPERATIONS_MAP_NAME);
        operationsMap.addLocalEntryListener(operationExpirationListener);
        return operationsMap;
    }

    @Bean
    public IMap<String, Topic> topicsMap(@Autowired @Qualifier("hazelcastInstance") HazelcastInstance hazelcastInstance) {
        return hazelcastInstance.getMap(TOPICS_MAP_NAME);
    }

    @PreDestroy
    public void destroy() {
        hazelcastInstance.shutdown();
    }
}
