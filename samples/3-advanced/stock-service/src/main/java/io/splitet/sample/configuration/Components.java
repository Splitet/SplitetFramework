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
import io.splitet.sample.model.Stock;
import io.splitet.sample.repository.StockRepository;
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
    private CassandraSession cassandraSession;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OperationContext operationContext;

    @Bean
    AggregateListener snapshotRecorder(
            ViewQuery<Stock> stockViewRepository,
            EventRepository stockEventRepository,
            StockRepository stockRepository,
            Optional<List<RollbackSpec>> rollbackSpecs
    ) {
        return new AggregateListener(stockViewRepository, stockEventRepository, stockRepository, rollbackSpecs.orElseGet(ArrayList::new), objectMapper);
    }

    @Bean
    ViewQuery<Stock> stockViewRepository(
            List<EntityFunctionSpec<Stock, ?>> functionSpecs, EventApisConfiguration eventApisConfiguration
    ) {
        return new CassandraViewQuery<>(
                eventApisConfiguration.getTableNameForEvents("stock"),
                cassandraSession, objectMapper, functionSpecs
        );
    }

    @Bean
    EventRecorder stockPersistentEventRepository(EventApisConfiguration eventApisConfiguration, IUserContext userContext) {
        return new CassandraEventRecorder(
                eventApisConfiguration.getTableNameForEvents("stock"),
                cassandraSession,
                operationContext,
                userContext,
                new ObjectMapper()
        );
    }

    @Bean
    EventRepository stockEventRepository(EventRecorder stockEventRecorder, IOperationRepository operationRepository) {
        return new CompositeRepositoryImpl(stockEventRecorder, new ObjectMapper(), operationRepository);
    }

}
