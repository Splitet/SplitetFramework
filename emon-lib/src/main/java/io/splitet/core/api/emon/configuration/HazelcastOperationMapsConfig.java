package io.splitet.core.api.emon.configuration;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapIndexConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import io.splitet.core.api.emon.domain.OperationEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.Arrays;
import java.util.List;

@Configuration
@Order
public class HazelcastOperationMapsConfig implements HazelcastConfigurer {

    public static final String EVENTS_OPS_MAP_NAME = "events-op";

    @Value("${emon.hazelcast.evict.freeHeapPercentage:10}")
    private Integer evictFreePercentage;

    @Override
    public Config configure(Config config) {

        List<MapIndexConfig> indexes = Arrays.asList(
                new MapIndexConfig("spanningServices[any].serviceName", true)
        );

        MapConfig mapConfig = new MapConfig(EVENTS_OPS_MAP_NAME)
                .setMapIndexConfigs(indexes)
                .setMaxSizeConfig(new MaxSizeConfig(evictFreePercentage, MaxSizeConfig.MaxSizePolicy.FREE_HEAP_PERCENTAGE))
                .setEvictionPolicy(EvictionPolicy.LRU)
                .setQuorumName("default")
                .setName(EVENTS_OPS_MAP_NAME);
        config.addMapConfig(mapConfig);
        return config;
    }

    @Bean
    public IMap<String, OperationEvents> eventsOperationsMap(@Autowired @Qualifier("hazelcastInstance") HazelcastInstance hazelcastInstance) {
        return hazelcastInstance.getMap(EVENTS_OPS_MAP_NAME);
    }
}
