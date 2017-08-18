package com.kloia.sample.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.api.EventRepository;
import com.kloia.eventapis.api.IUserContext;
import com.kloia.eventapis.api.RollbackSpec;
import com.kloia.eventapis.api.ViewQuery;
import com.kloia.eventapis.cassandra.CassandraEventRecorder;
import com.kloia.eventapis.cassandra.CassandraSession;
import com.kloia.eventapis.cassandra.CassandraViewQuery;
import com.kloia.eventapis.common.EventRecorder;
import com.kloia.eventapis.common.OperationContext;
import com.kloia.eventapis.core.CompositeRepositoryImpl;
import com.kloia.eventapis.kafka.IOperationRepository;
import com.kloia.eventapis.spring.configuration.EventApisConfiguration;
import com.kloia.eventapis.view.AggregateListener;
import com.kloia.eventapis.view.EntityFunctionSpec;
import com.kloia.sample.model.Order;
import com.kloia.sample.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    AggregateListener snapshotRecorder(ViewQuery<Order> orderViewRepository, EventRepository orderEventRepository, OrderRepository orderRepository,
                                       Optional<List<RollbackSpec>> rollbackSpecs) {
        return new AggregateListener(orderViewRepository, orderEventRepository, orderRepository, rollbackSpecs.orElseGet(ArrayList::new), objectMapper);
    }

    @Bean
    ViewQuery<Order> orderViewRepository(List<EntityFunctionSpec<Order, ?>> functionSpecs,EventApisConfiguration eventApisConfiguration) {
        return new CassandraViewQuery<>(
                eventApisConfiguration.getTableNameForEvents("order"),
                cassandraSession, objectMapper, functionSpecs);
    }

    @Bean
    EventRecorder orderPersistentEventRepository(EventApisConfiguration eventApisConfiguration,IUserContext userContext) {
        return new CassandraEventRecorder(eventApisConfiguration.getTableNameForEvents("order"), cassandraSession, operationContext, userContext, new ObjectMapper());
    }

    @Bean
    EventRepository orderEventRepository(EventRecorder orderEventRecorder, IOperationRepository operationRepository, IUserContext userContext) {
        return new CompositeRepositoryImpl(orderEventRecorder, operationContext, new ObjectMapper(), operationRepository, userContext);
    }



}
