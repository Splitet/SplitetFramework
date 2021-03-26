package io.splitet.core.api.emon.configuration.hazelcast;

import com.hazelcast.config.Config;
import io.splitet.core.api.emon.configuration.HazelcastConfigurer;
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
public class MulticastConfig extends com.hazelcast.config.MulticastConfig implements HazelcastConfigurer {
    @Override
    public Config configure(Config config) {
        config.getNetworkConfig().getJoin().setMulticastConfig(this);
        return config;
    }
}
