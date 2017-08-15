package com.kloia.sample.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.common.OperationContext;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.kafka.IOperationRepository;

import com.kloia.eventapis.kafka.KafkaOperationRepositoryFactory;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.pojos.TransactionState;
import com.kloia.eventapis.spring.configuration.EventApisConfiguration;
import com.kloia.eventapis.cassandra.CassandraEventRecorder;
import com.kloia.eventapis.cassandra.CassandraSession;
import com.kloia.eventapis.view.EntityFunctionSpec;
import com.kloia.eventapis.api.EventRepository;
import com.kloia.eventapis.core.CompositeRepositoryImpl;
import com.kloia.eventapis.common.EventRecorder;
import com.kloia.eventapis.api.IUserContext;
import com.kloia.eventapis.api.ViewQuery;
import com.kloia.eventapis.cassandra.QueryByPersistentEventRepositoryDelegate;
import com.kloia.sample.model.Stock;
import com.kloia.sample.repository.StockRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.List;
import java.util.Map;

@Configuration
@Slf4j
public class Components {

    @Autowired
    private EventApisConfiguration eventApisConfiguration;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OperationContext operationContext;


    @Bean
    EventRecorder<Stock> orderRepository(List<EntityFunctionSpec<Stock, ?>> orderFunctionSpecs){
        CassandraSession cassandraSession = new CassandraSession(eventApisConfiguration.getStoreConfig());
        CassandraEventRecorder<Stock> cassandraEventRepository = new CassandraEventRecorder<>(eventApisConfiguration.getTableNames().getOrDefault("stockevents", "stockevents"), cassandraSession, objectMapper);
        cassandraEventRepository.addCommandSpecs(orderFunctionSpecs);
        return cassandraEventRepository;
    }
    @Bean
    EventRepository<Stock> orderEventRepository(EventRecorder<Stock> orderEventRecorder, IOperationRepository operationRepository, IUserContext userContext){

        return new CompositeRepositoryImpl(orderEventRecorder, operationContext, new ObjectMapper(), operationRepository, userContext);
    }
    @Bean
    IOperationRepository kafkaOperationRepository(){
        KafkaOperationRepositoryFactory kafka = new KafkaOperationRepositoryFactory(eventApisConfiguration.getEventBus());
       return kafka.createKafkaOperationRepository(objectMapper);
    }


    @Bean
    ViewQuery<Stock> orderQuery(EventRecorder<Stock> orderEventRecorder){
        return new QueryByPersistentEventRepositoryDelegate<>(orderEventRecorder);
    }

/*    @Bean
    @Scope("prototype")
    public Feign.Builder feignBuilder() {
        return Feign.builder();
    }*/

    @Autowired
    private ApplicationContext applicationContext;



    @Autowired
    private StockRepository stockRepository;


    @KafkaListener(id = "op-listener", topics = "operation-events", containerFactory = "operationsKafkaListenerContainerFactory")
    private void listenOperations(ConsumerRecord<String, Operation> data) throws EventStoreException {
        EventRecorder<Stock> eventRepository = applicationContext.getBean(EventRecorder.class);
        log.warn("Incoming Message: " + data.value());
        if (data.value().getTransactionState() == TransactionState.TXN_FAILED) {
            eventRepository.markFail(data.key());
            Stock order = eventRepository.queryEntity(data.key());
            if(order != null && order.getId() != null)
                stockRepository.save(order);
        }else if (data.value().getTransactionState() == TransactionState.TXN_SUCCEDEED) {
            eventRepository.queryByOpId(data.key()).stream().forEach(stockRepository::save);
        }

    }

    @Bean
    public IUserContext getUserContext() {
        return new EmptyUserContext();
    }

    private static class EmptyUserContext implements IUserContext {
        @Override
        public Map<String, String> getUserContext() {
            return null;
        }

        @Override
        public void extractUserContext(Map<String, String> userContext) {

        }
    }


/*    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }*/
}
