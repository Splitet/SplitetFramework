package com.kloia.eventapis.api.emon.configuration.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.config.InterfacesConfig;
import com.kloia.eventapis.api.emon.configuration.HazelcastConfigurer;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConditionalOnProperty(value = "emon.hazelcast.interfaces.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "emon.hazelcast.interfaces")
public class NetworkInterfacesConfig extends InterfacesConfig implements HazelcastConfigurer {

    @Override
    public Config configure(Config config) {
        config.getNetworkConfig().setInterfaces(this);
        return config;
    }
}
