package com.kloia.eventapis.api.emon.configuration;

import com.hazelcast.config.MulticastConfig;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConditionalOnProperty(value = "emon.hazelcast.discovery.type", havingValue = "multicast")
@ConfigurationProperties(prefix = "emon.hazelcast.discovery.multicast")
public class InMemoryConfig extends MulticastConfig {

    /*
    *    <multicast enabled="true">
                <multicast-group>224.2.2.3</multicast-group>
                <multicast-port>54327</multicast-port>
                <multicast-time-to-live>32</multicast-time-to-live>
                <multicast-timeout-seconds>2</multicast-timeout-seconds>
                <trusted-interfaces>
                   <interface>192.168.1.102</interface>
                </trusted-interfaces>
            </multicast>
            */

}
