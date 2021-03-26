package io.splitet.core.api.emon.configuration.hazelcast;

import com.hazelcast.config.Config;
import io.splitet.core.api.emon.configuration.HazelcastConfigurer;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConditionalOnProperty(value = "emon.hazelcast.user-code-deployment.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "emon.hazelcast.user-code-deployment")
public class UserCodeDeploymentConfig extends com.hazelcast.config.UserCodeDeploymentConfig implements HazelcastConfigurer {

    @Override
    public Config configure(Config config) {
        config.setUserCodeDeploymentConfig(this);
        return config;
    }
}
