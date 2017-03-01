package com.kloia.eventbus;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;

/**
 * Created by zeldalozdemir on 26/02/2017.
 */
@Configuration
public class EventBusConfig {

    @Value("eventbus.servers")
    private String kafkaServerAddresses;

    @Bean
    public KafkaTemplate<Integer, String> kafkaTemplate() {
        HashMap<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,kafkaServerAddresses);
        return new KafkaTemplate<Integer, String>(new DefaultKafkaProducerFactory<>(configs));
    }

}
