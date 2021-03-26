package io.splitet.core.spring.configuration;

import io.splitet.core.cassandra.EventStoreConfig;
import io.splitet.core.kafka.KafkaProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@ConfigurationProperties("eventapis")
@Component
@Slf4j
@Data
public class EventApisConfiguration {
    private EventStoreConfig storeConfig;
    private KafkaProperties eventBus;
    private Map<String, String> eventRecords;
    private String baseEventsPackage;

    public String getTableNameForEvents(String eventName) {
        return getEventRecords().getOrDefault(eventName, eventName);
    }
}


