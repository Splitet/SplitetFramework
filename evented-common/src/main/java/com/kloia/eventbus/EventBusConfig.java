package com.kloia.eventbus;

import com.kloia.eventapis.pojos.Operation;
import com.kloia.evented.Command;
import com.kloia.evented.CommandExecutionInterceptor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 26/02/2017.
 */
@Configuration
@EnableKafka
@PropertySources({
        @PropertySource("classpath:application.properties"),
        @PropertySource("classpath:project.properties")
})
public class EventBusConfig {

    @Value("${eventbus.servers}")
    private String kafkaServerAddresses;


    @Value("${artifactId}")
    private String artifactId;


    @Bean
    public Map producerConfigs() {
        Map props = new HashMap<>();
        // list of host:port pairs used for establishing the initial connections
        // to the Kakfa cluster
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafkaServerAddresses);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class);
        // value to block, after which it will throw a TimeoutException
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5000);

        return props;
    }

    @Bean
    public ProducerFactory<UUID, Operation> producerFactory() {
        return new DefaultKafkaProducerFactory<UUID, Operation>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<UUID, Operation> kafkaTemplate() {
        return new KafkaTemplate<UUID, Operation>(producerFactory());
    }


    @Bean
    public Map consumerConfigs() {
        Map props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafkaServerAddresses);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, artifactId);
//        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "operations-client");
//        props.put(ConsumerConfig., "operations");

        return props;
    }

    @Bean
    public ConsumerFactory<UUID, Operation> consumerFactory() {
        return new DefaultKafkaConsumerFactory<UUID, Operation>(consumerConfigs(),new JsonDeserializer<UUID>(UUID.class),new JsonDeserializer(Operation.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<UUID, Operation> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<UUID, Operation> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }



}
