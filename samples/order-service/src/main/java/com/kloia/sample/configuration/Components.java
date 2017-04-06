package com.kloia.sample.configuration;

import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.pojos.TransactionState;
import com.kloia.evented.CassandraEventRepository;
import com.kloia.evented.IEventRepository;
import com.kloia.sample.dto.Order;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.UUID;

@Configuration
@Slf4j
public class Components {


    @Bean("orderEventRepository")
    public IEventRepository<Order> createOrderEventRepository(@Autowired CassandraTemplate cassandraTemplate) {
        return new CassandraEventRepository<Order>("OrderEvents", cassandraTemplate);
    }

/*    @Bean
    @Scope("prototype")
    public Feign.Builder feignBuilder() {
        return Feign.builder();
    }*/

    @Autowired
    private ApplicationContext applicationContext;


    @KafkaListener(id = "op-listener", topics = "operation-events")
    private void listenOperations(ConsumerRecord<UUID, Operation> data) {
        IEventRepository paymentEventRepository = applicationContext.getBean(IEventRepository.class);
        log.warn("Incoming Message: " + data.value());
        if (data.value().getTransactionState() == TransactionState.TXN_FAILED) {
            paymentEventRepository.markFail(data.key());
        }
    }

/*    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }*/
}
