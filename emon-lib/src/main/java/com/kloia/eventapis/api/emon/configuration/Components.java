package com.kloia.eventapis.api.emon.configuration;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapIndexConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.spring.context.SpringManagedContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class Components {

    public static final int OPERATIONS_MAX_TTL_INSEC = 60000;
    public static final String OPERATIONS_MAP_NAME = "operations";
    public static final String TOPICS_MAP_NAME = "topics";

    @Value("${emon.hazelcast.group.name:'emon'}")
    private String hazelcastGrid;
    @Value("${emon.hazelcast.group.password:'emon123'}")
    private String hazelcastPassword;

    @Autowired(required = false)
    private InMemoryConfig inMemoryConfig;

    @Autowired(required = false)
    private InMemoryInterfacesConfig inMemoryInterfacesConfig;

    @Value("${eventapis.eventBus.consumer.groupId}")
//    @Value("${info.build.artifact}")
    private String artifactId;

    @Bean
    public Config config() {
        MapConfig mapConfig = new MapConfig();
        mapConfig.setTimeToLiveSeconds(OPERATIONS_MAX_TTL_INSEC);
        mapConfig.setMapIndexConfigs(
                Arrays.asList(
                        new MapIndexConfig("startTime", true),
                        new MapIndexConfig("operationState", true)
                )
        );
        Map<String, MapConfig> mapConfigs = new HashMap<>();
        mapConfigs.put(OPERATIONS_MAP_NAME, mapConfig);
        mapConfigs.put(TOPICS_MAP_NAME, new MapConfig());
        Config config = new Config();
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
        config.setGroupConfig(groupConfig);
        config.setInstanceName(artifactId);
        config.setMapConfigs(mapConfigs);
        return config;
    }

    //    @Bean
//    @Primary
//    @DependsOn({"adminClient","adminToolsClient"})
//    public HazelcastInstance hazelcastInstance(Config config) {
//        return new HazelcastInstanceFactory(config).getHazelcastInstance();
//    }
    @Bean
    public SpringManagedContext managedContext() {
        return new SpringManagedContext();
    }

    @Bean
    @Primary
    @DependsOn({"adminClient", "adminToolsClient"})
    public HazelcastInstance hazelcastInstance(Config config, SpringManagedContext springManagedContext) {
//        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(SpringManagedContext.class);
//        builder.setScope("prototype");
//        builder.setLazyInit(true);
//        config.addPropertyValue("managedContext", builder.getBeanDefinition());
//        springManagedContext.set
        config.setManagedContext(springManagedContext);
        return Hazelcast.newHazelcastInstance(config);
    }

    @Bean
    public IMap operationsMap(@Autowired @Qualifier("hazelcastInstance") HazelcastInstance hazelcastInstance) {
        return hazelcastInstance.getMap(OPERATIONS_MAP_NAME);
    }

    @Bean
    public IMap topicsMap(@Autowired @Qualifier("hazelcastInstance") HazelcastInstance hazelcastInstance) {
        return hazelcastInstance.getMap(TOPICS_MAP_NAME);
    }
}
