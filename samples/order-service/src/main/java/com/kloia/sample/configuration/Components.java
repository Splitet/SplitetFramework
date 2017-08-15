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
import com.kloia.eventapis.kafka.IOperationRepository;
import com.kloia.eventapis.spring.configuration.EventApisConfiguration;
import com.kloia.eventapis.view.EntityFunctionSpec;
import com.kloia.eventapis.view.SnapshotRecorder;
import com.kloia.sample.model.Order;
import com.kloia.sample.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Slf4j
public class Components {

    @Autowired
    CassandraSession cassandraSession;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OperationContext operationContext;

    @Bean
    SnapshotRecorder snapshotRecorder(ViewQuery<Order> orderViewRepository,EventRepository orderEventRepository,OrderRepository orderRepository){
        return new SnapshotRecorder(orderViewRepository,orderEventRepository, orderRepository );
    }

    @Bean
    ViewQuery<Order> orderViewRepository(List<EntityFunctionSpec<Order, ?>> functionSpecs,EventApisConfiguration eventApisConfiguration) {
        return new CassandraViewQuery<>(
                eventApisConfiguration.getTableNameForEvents("order"),
                cassandraSession, objectMapper, functionSpecs);
    }

    @Bean
    EventRecorder<Order> orderPersistentEventRepository(EventApisConfiguration eventApisConfiguration) {
        return new CassandraEventRecorder<>(eventApisConfiguration.getTableNameForEvents("order"), cassandraSession, objectMapper);
    }

    @Bean
    EventRepository orderEventRepository(EventRecorder<Order> orderEventRecorder, IOperationRepository operationRepository, IUserContext userContext) {
        return new CompositeRepositoryImpl(orderEventRecorder, operationContext, new ObjectMapper(), operationRepository, userContext);
    }



}
