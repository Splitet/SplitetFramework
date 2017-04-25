package com.kloia.eventbus;

<<<<<<< HEAD
<<<<<<< HEAD
import com.kloia.eventapis.pojos.Operation;
import com.kloia.evented.Command;
import com.kloia.evented.CommandExecutionInterceptor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
=======
>>>>>>> 5cb7fff...  - Event bus implementation examples
=======
import com.kloia.eventapis.pojos.Operation;
import org.apache.kafka.clients.consumer.ConsumerConfig;
>>>>>>> dc25faf...  - Event bus implementation examples
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Value;
<<<<<<< HEAD
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> dc25faf...  - Event bus implementation examples
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
=======
import org.springframework.context.annotation.*;
>>>>>>> 939bca8...  - Event api implementation examples
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
<<<<<<< HEAD

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
=======
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
=======
>>>>>>> dc25faf...  - Event bus implementation examples

import javax.annotation.Resource;
import java.util.HashMap;
<<<<<<< HEAD
>>>>>>> 5cb7fff...  - Event bus implementation examples
=======
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
>>>>>>> dc25faf...  - Event bus implementation examples

/**
 * Created by zeldalozdemir on 26/02/2017.
 */
@Configuration
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> dc25faf...  - Event bus implementation examples
@EnableKafka
@PropertySources({
        @PropertySource("classpath:application.properties"),
        @PropertySource("classpath:project.properties")
})
<<<<<<< HEAD
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
=======
=======
>>>>>>> dc25faf...  - Event bus implementation examples
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
<<<<<<< HEAD
    public KafkaTemplate<Integer, String> kafkaTemplate() {
        HashMap<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,kafkaServerAddresses);
        return new KafkaTemplate<Integer, String>(new DefaultKafkaProducerFactory<>(configs));
>>>>>>> 5cb7fff...  - Event bus implementation examples
=======
    public ConcurrentKafkaListenerContainerFactory<UUID, Operation> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<UUID, Operation> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
>>>>>>> dc25faf...  - Event bus implementation examples
    }



}
