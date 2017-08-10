package com.kloia.eventapis.configuration;

import com.kloia.eventapis.api.impl.KafkaProperties;
import com.kloia.evented.EventStoreConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties("eventapis")
public class EventApisConfiguration {
    private EventStoreConfig storeConfig;
    private KafkaProperties eventBus;
    private Map<String, String> tableNames;
}
