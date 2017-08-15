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
import com.kloia.sample.model.Payment;
import com.kloia.sample.repository.PaymentRepository;
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
    SnapshotRecorder snapshotRecorder(ViewQuery<Payment> paymentViewRepository, EventRepository paymentEventRepository, PaymentRepository paymentRepository){
        return new SnapshotRecorder(paymentViewRepository,paymentEventRepository, paymentRepository );
    }

    @Bean
    ViewQuery<Payment> paymentViewRepository(List<EntityFunctionSpec<Payment, ?>> functionSpecs,EventApisConfiguration eventApisConfiguration) {
        return new CassandraViewQuery<>(
                eventApisConfiguration.getTableNameForEvents("payment"),
                cassandraSession, objectMapper, functionSpecs);
    }

    @Bean
    EventRecorder<Payment> paymentPersistentEventRepository(EventApisConfiguration eventApisConfiguration) {
        return new CassandraEventRecorder<>(eventApisConfiguration.getTableNameForEvents("payment"), cassandraSession, objectMapper);
    }

    @Bean
    EventRepository paymentEventRepository(EventRecorder<Payment> paymentEventRecorder, IOperationRepository operationRepository, IUserContext userContext) {
        return new CompositeRepositoryImpl(paymentEventRecorder, operationContext, new ObjectMapper(), operationRepository, userContext);
    }

}
