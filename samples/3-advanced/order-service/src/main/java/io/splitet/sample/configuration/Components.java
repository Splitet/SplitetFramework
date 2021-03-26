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
import io.splitet.core.spring.configuration.DataMigrationService;
import io.splitet.core.spring.configuration.EventApisConfiguration;
import io.splitet.core.view.AggregateListener;
import io.splitet.core.view.EntityFunctionSpec;
import io.splitet.sample.model.Orders;
import io.splitet.sample.repository.OrderRepository;
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
    AggregateListener snapshotRecorder(ViewQuery<Orders> orderViewRepository, // View Query with Function Specs
                                       EventRepository orderEventRepository, // Event Repository to mark failed events
                                       OrderRepository orderRepository,      // Jpa Repository to record snapshots
                                       Optional<List<RollbackSpec>> rollbackSpecs // Custom Rollback Specs for Event Failures
    ) {
        return new AggregateListener(orderViewRepository, orderEventRepository, orderRepository, rollbackSpecs.orElseGet(ArrayList::new), objectMapper);
    }

    @Bean
    ViewQuery<Orders> orderViewRepository(List<EntityFunctionSpec<Orders, ?>> functionSpecs, EventApisConfiguration eventApisConfiguration) {
        return new CassandraViewQuery<>(
                eventApisConfiguration.getTableNameForEvents("order"),
                cassandraSession, objectMapper, functionSpecs);
    }

    @Bean
    EventRecorder orderPersistentEventRepository(EventApisConfiguration eventApisConfiguration, IUserContext userContext) {
        return new CassandraEventRecorder(eventApisConfiguration.getTableNameForEvents("order"), cassandraSession, operationContext, userContext, new ObjectMapper());
    }

    @Bean
    EventRepository orderEventRepository(EventRecorder orderEventRecorder, IOperationRepository operationRepository) {
        return new CompositeRepositoryImpl(orderEventRecorder, new ObjectMapper(), operationRepository);
    }

    @Bean
    DataMigrationService dataMigrationService(EventRecorder orderEventRecorder, ViewQuery<Orders> orderViewQuery, OrderRepository orderRepository) {
        return new DataMigrationService(orderEventRecorder, orderViewQuery, orderRepository);
    }


}
