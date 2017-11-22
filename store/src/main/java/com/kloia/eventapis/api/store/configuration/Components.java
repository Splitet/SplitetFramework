package com.kloia.eventapis.api.store.configuration;

import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.NetworkConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class Components {

    public static final int TIME_TO_LIVE_SECONDS = 60000;
    public static final String OPERATIONS_MAP_NAME = "operations";

    @Value("${eventapis.eventBus.consumer.groupId}")
    private String artifactId;

    @Bean
    public Config config() {
        //ManagementCenterConfig manCenterCfg = new ManagementCenterConfig();
        //manCenterCfg.setEnabled(true).setUrl("http://localhost:8080/mancenter");
        MapConfig mapConfig = new MapConfig();
        mapConfig.setTimeToLiveSeconds(TIME_TO_LIVE_SECONDS);
        Map<String, MapConfig> mapConfigs = new HashMap<>();
        mapConfigs.put(OPERATIONS_MAP_NAME, mapConfig);
//        mapConfigs.put("events-resources", mapConfig);
        Config config = new Config();
        /*
        config.setExecutorConfigs(Collections.singletonMap("default",new ExecutorConfig("default",2)));
        config.setProperty("hazelcast.event.thread.count","2");
        config.setProperty("hazelcast.operation.generic.thread.count","2");
        config.setProperty("hazelcast.operation.thread.count","2");
        config.setProperty("hazelcast.io.thread.count","1");
        config.setProperty("hazelcast.logging.type", "slf4j");
        */
        NetworkConfig networkConfig = new NetworkConfig();
        JoinConfig join = new JoinConfig();
        DiscoveryConfig discoveryConfig = new DiscoveryConfig();
        join.setDiscoveryConfig(discoveryConfig);
        networkConfig.setJoin(join);
        config.setNetworkConfig(networkConfig);
        GroupConfig groupConfig = new GroupConfig();
        groupConfig.setName("op-center");
        config.setGroupConfig(groupConfig);
        config.setInstanceName("op-center");
        config.setMapConfigs(mapConfigs);
        return config;
    }
}
