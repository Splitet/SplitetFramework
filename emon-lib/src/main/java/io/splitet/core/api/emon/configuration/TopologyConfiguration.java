package io.splitet.core.api.emon.configuration;

import io.splitet.core.kafka.JsonDeserializer;
import io.splitet.core.pojos.Operation;
import io.splitet.core.spring.configuration.EventApisConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.Properties;

@Configuration
@Import(EventApisConfiguration.class)
@Slf4j
@ConditionalOnProperty(value = "emon.offsetScheduler.enabled", havingValue = "true")
public class TopologyConfiguration {

    @Autowired
    private EventApisConfiguration eventApisConfiguration;


    @Bean(name = "kafkaAdminProperties")
    public Properties kafkaAdminProperties() {
        String bootstrapServers = String.join(",", eventApisConfiguration.getEventBus().getBootstrapServers());
        String zookeeperServers = String.join(",", eventApisConfiguration.getEventBus().getZookeeperServers());
        Properties properties = new Properties();
        properties.putAll(eventApisConfiguration.getEventBus().buildCommonProperties());
        properties.put("zookeeper", zookeeperServers);
        properties.put("bootstrap.servers", bootstrapServers);
        return properties;
    }

    @Bean("adminClient")
    public AdminClient adminClient(@Autowired @Qualifier("kafkaAdminProperties") Properties kafkaAdminProperties) {
        return AdminClient.create(kafkaAdminProperties);
    }

    @Bean
    public Consumer<String, Operation> kafkaConsumer() {
        return new DefaultKafkaConsumerFactory<>(
                eventApisConfiguration.getEventBus().buildConsumerProperties(),
                new StringDeserializer(),
                new JsonDeserializer<>(Operation.class)).createConsumer();
    }


}
