package io.splitet.core.api.emon.configuration;

import com.hazelcast.config.Config;

public interface HazelcastConfigurer {

    Config configure(Config existingConfig);
}
