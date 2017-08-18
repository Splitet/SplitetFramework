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
import com.kloia.eventapis.view.AggregateListener;
import com.kloia.eventapis.view.EntityFunctionSpec;
import com.kloia.eventapis.view.RollbackSpec;
import com.kloia.sample.model.Payment;
import com.kloia.sample.repository.PaymentRepository;
import com.sun.javafx.scene.control.skin.VirtualFlow;
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
    AggregateListener snapshotRecorder(ViewQuery<Payment> paymentViewRepository, EventRepository paymentEventRepository, PaymentRepository paymentRepository,
                                       Optional<List<RollbackSpec>> rollbackSpecs) {
        return new AggregateListener(paymentViewRepository, paymentEventRepository, paymentRepository, rollbackSpecs.orElseGet(ArrayList::new));
    }

    @Bean
    ViewQuery<Payment> paymentViewRepository(List<EntityFunctionSpec<Payment, ?>> functionSpecs,EventApisConfiguration eventApisConfiguration) {
        return new CassandraViewQuery<>(
                eventApisConfiguration.getTableNameForEvents("payment"),
                cassandraSession, objectMapper, functionSpecs);
    }

    @Bean
    EventRecorder paymentPersistentEventRepository(EventApisConfiguration eventApisConfiguration, IUserContext userContext) {
        return new CassandraEventRecorder(eventApisConfiguration.getTableNameForEvents("payment"), cassandraSession, operationContext, userContext, new ObjectMapper());
    }

    @Bean
    EventRepository paymentEventRepository(EventRecorder paymentEventRecorder, IOperationRepository operationRepository, IUserContext userContext) {
        return new CompositeRepositoryImpl(paymentEventRecorder, operationContext, new ObjectMapper(), operationRepository, userContext);
    }

}
