package com.kloia.eventapis.api.emon.configuration;

import com.hazelcast.config.InterfacesConfig;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConditionalOnProperty(value = "emon.hazelcast.interfaces.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "emon.hazelcast.interfaces")
public class InMemoryInterfacesConfig extends InterfacesConfig {


}
