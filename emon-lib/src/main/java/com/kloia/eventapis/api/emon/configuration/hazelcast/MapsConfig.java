package com.kloia.eventapis.api.emon.configuration.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapIndexConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import com.kloia.eventapis.api.emon.configuration.HazelcastConfigurer;
import com.kloia.eventapis.api.emon.domain.Topic;
import com.kloia.eventapis.api.emon.domain.Topology;
import com.kloia.eventapis.api.emon.service.OperationExpirationListener;
import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Data
@Configuration
@ToString(callSuper = true)
public class MapsConfig extends MulticastConfig implements HazelcastConfigurer {
    public static final int OPERATIONS_MAX_TTL_INSEC = 60000;
    public static final String OPERATIONS_MAP_NAME = "operations";
    public static final String OPERATIONS_MAP_HISTORY_NAME = "operations-history";
    public static final String META_MAP_NAME = "meta";
    public static final String TOPICS_MAP_NAME = "topics";
    public static final String OPERATIONS_TOPICS_NAME = "operations_topic";

    @Value("${emon.hazelcast.evict.freeHeapPercentage:20}")
    private Integer evictFreePercentage;

    @Override
    public Config configure(Config config) {

        List<MapIndexConfig> indexes = Arrays.asList(
                new MapIndexConfig("startTime", true),
                new MapIndexConfig("operationState", true)
        );
        config.getMapConfig(OPERATIONS_MAP_NAME)
                .setTimeToLiveSeconds(OPERATIONS_MAX_TTL_INSEC)
                .setMapIndexConfigs(indexes);

        config.getMapConfig(OPERATIONS_MAP_HISTORY_NAME)
                .setMapIndexConfigs(indexes)
                .setMaxSizeConfig(new MaxSizeConfig(evictFreePercentage, MaxSizeConfig.MaxSizePolicy.FREE_HEAP_PERCENTAGE))
                .setEvictionPolicy(EvictionPolicy.LRU);
        config.getReplicatedMapConfig(TOPICS_MAP_NAME);

        config.getTopicConfig(OPERATIONS_TOPICS_NAME);

        return config;
    }

    @Bean
    @Qualifier("operationsMap")
    public IMap<String, Topology> operationsMap(@Autowired @Qualifier("hazelcastInstance") HazelcastInstance hazelcastInstance, OperationExpirationListener operationExpirationListener) {
        IMap<String, Topology> operationsMap = hazelcastInstance.getMap(OPERATIONS_MAP_NAME);
        operationsMap.addLocalEntryListener(operationExpirationListener);
        return operationsMap;
    }

    @Bean
    @Qualifier("operationsHistoryMap")
    public IMap<String, Topology> operationsHistoryMap(@Autowired @Qualifier("hazelcastInstance") HazelcastInstance hazelcastInstance) {
        return hazelcastInstance.getMap(OPERATIONS_MAP_HISTORY_NAME);
    }

    @Bean
    @Qualifier("metaMap")
    public IMap<String, Object> metaMap(@Autowired @Qualifier("hazelcastInstance") HazelcastInstance hazelcastInstance) {
        return hazelcastInstance.getMap(META_MAP_NAME);
    }

    @Bean
    @Qualifier("topicsMap")
    public IMap<String, Topic> topicsMap(@Autowired @Qualifier("hazelcastInstance") HazelcastInstance hazelcastInstance) {
        return hazelcastInstance.getMap(TOPICS_MAP_NAME);
    }

    @Bean
    @Qualifier("operationsTopic")
    public ITopic<Topology> operationsTopic(@Autowired @Qualifier("hazelcastInstance") HazelcastInstance hazelcastInstance) {
        return hazelcastInstance.getTopic(OPERATIONS_TOPICS_NAME);
    }
}
