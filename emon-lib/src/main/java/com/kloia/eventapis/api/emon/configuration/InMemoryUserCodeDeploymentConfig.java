package com.kloia.eventapis.api.emon.configuration;

import com.hazelcast.config.UserCodeDeploymentConfig;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConditionalOnProperty(value = "emon.hazelcast.user-code-deployment.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "emon.hazelcast.user-code-deployment")
public class InMemoryUserCodeDeploymentConfig extends UserCodeDeploymentConfig {
}
