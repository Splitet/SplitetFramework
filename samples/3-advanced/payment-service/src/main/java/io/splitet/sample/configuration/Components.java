package io.splitet.sample.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.splitet.core.api.EventRepository;
import io.splitet.core.api.IUserContext;
import io.splitet.core.api.RollbackSpec;
import io.splitet.core.api.ViewQuery;
import io.splitet.core.cassandra.CassandraEventRecorder;
import io.splitet.core.cassandra.CassandraSession;
import io.splitet.core.cassandra.CassandraViewQuery;
import io.splitet.core.common.EventRecorder;
import io.splitet.core.common.OperationContext;
import io.splitet.core.core.CompositeRepositoryImpl;
import io.splitet.core.kafka.IOperationRepository;
import io.splitet.core.spring.configuration.EventApisConfiguration;
import io.splitet.core.view.AggregateListener;
import io.splitet.core.view.EntityFunctionSpec;
import io.splitet.sample.model.Payment;
import io.splitet.sample.repository.PaymentRepository;
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
    AggregateListener snapshotRecorder(ViewQuery<Payment> paymentViewQuery, EventRepository paymentEventRepository, PaymentRepository paymentRepository,
                                       Optional<List<RollbackSpec>> rollbackSpecs) {
        return new AggregateListener(paymentViewQuery, paymentEventRepository, paymentRepository, rollbackSpecs.orElseGet(ArrayList::new), objectMapper);
    }

    @Bean
    ViewQuery<Payment> paymentViewQuery(List<EntityFunctionSpec<Payment, ?>> functionSpecs, EventApisConfiguration eventApisConfiguration) {
        return new CassandraViewQuery<>(
                eventApisConfiguration.getTableNameForEvents("payment"),
                cassandraSession, objectMapper, functionSpecs);
    }

    @Bean
    EventRecorder paymentPersistentEventRepository(EventApisConfiguration eventApisConfiguration, IUserContext userContext) {
        return new CassandraEventRecorder(eventApisConfiguration.getTableNameForEvents("payment"), cassandraSession, operationContext, userContext, new ObjectMapper());
    }

    @Bean
    EventRepository paymentEventRepository(EventRecorder paymentEventRecorder, IOperationRepository operationRepository) {
        return new CompositeRepositoryImpl(paymentEventRecorder, new ObjectMapper(), operationRepository);
    }

}
