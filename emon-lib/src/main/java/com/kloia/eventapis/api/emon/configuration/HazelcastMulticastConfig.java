package com.kloia.eventapis.api.emon.configuration;

import com.hazelcast.config.Config;
import com.hazelcast.config.MulticastConfig;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConditionalOnProperty(value = "emon.hazelcast.discovery.type", havingValue = "multicast")
@ConfigurationProperties(prefix = "emon.hazelcast.discovery.multicast")
@ToString(callSuper = true)
public class HazelcastMulticastConfig extends MulticastConfig implements HazelcastConfigurer {
    @Override
    public Config configure(Config config) {
        config.getNetworkConfig().getJoin().setMulticastConfig(this);
        return config;
    }
}
