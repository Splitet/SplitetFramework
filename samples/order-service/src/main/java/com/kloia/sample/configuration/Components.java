package com.kloia.sample.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.api.EventRepository;
import com.kloia.eventapis.api.IUserContext;
import com.kloia.eventapis.api.ViewQuery;
import com.kloia.eventapis.cassandra.CassandraEventRecorder;
import com.kloia.eventapis.cassandra.CassandraSession;
import com.kloia.eventapis.cassandra.CassandraViewQuery;
import com.kloia.eventapis.common.EventRecorder;
import com.kloia.eventapis.common.OperationContext;
import com.kloia.eventapis.core.CompositeRepositoryImpl;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.kafka.IOperationRepository;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.pojos.TransactionState;
import com.kloia.eventapis.spring.configuration.EventApisConfiguration;
import com.kloia.eventapis.view.EntityFunctionSpec;
import com.kloia.eventapis.view.SnapshotRecorder;
import com.kloia.eventapis.view.SnapshotRepository;
import com.kloia.sample.model.Order;
import com.kloia.sample.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.List;

@Configuration
@Slf4j
@Import(EventApisConfiguration.class)
public class Components {

    @Autowired
    private EventApisConfiguration eventApisConfiguration;


    @Autowired
    CassandraSession cassandraSession;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OperationContext operationContext;
    @Autowired


    @Bean
    SnapshotRecorder snapshotRecorder(ViewQuery<Order> orderViewRepository,EventRepository orderEventRepository,OrderRepository orderRepository){
        return new SnapshotRecorder(orderViewRepository,orderEventRepository, orderRepository );
    }

    @Bean
    ViewQuery<Order> orderViewRepository(List<EntityFunctionSpec<Order, ?>> functionSpecs) {
        return new CassandraViewQuery<>(
                eventApisConfiguration.getTableNames().getOrDefault("orderevents", "orderevents"),
                cassandraSession, objectMapper, functionSpecs);
    }

    @Bean
    EventRecorder<Order> orderPersistentEventRepository() {
        return new CassandraEventRecorder<>(eventApisConfiguration.getTableNames().getOrDefault("orderevents", "orderevents"), cassandraSession, objectMapper);
    }

    @Bean
    EventRepository orderEventRepository(EventRecorder<Order> orderEventRecorder, IOperationRepository operationRepository, IUserContext userContext) {
        return new CompositeRepositoryImpl(orderEventRecorder, operationContext, new ObjectMapper(), operationRepository, userContext);
    }



}
