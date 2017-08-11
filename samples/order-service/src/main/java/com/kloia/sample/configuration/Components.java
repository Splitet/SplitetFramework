package com.kloia.sample.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.common.OperationContext;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.kafka.IOperationRepository;
import com.kloia.eventapis.kafka.KafkaOperationRepositoryFactory;
import com.kloia.eventapis.spring.configuration.EventApisConfiguration;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.pojos.TransactionState;
import com.kloia.eventapis.cassandra.CassandraEventRepository;
import com.kloia.eventapis.cassandra.CassandraSession;
import com.kloia.eventapis.view.Entity;
import com.kloia.eventapis.view.EntityFunctionSpec;
import com.kloia.eventapis.api.EventRepository;
import com.kloia.eventapis.core.CompositeRepositoryImpl;
import com.kloia.eventapis.cassandra.PersistentEventRepository;
import com.kloia.eventapis.api.IUserContext;
import com.kloia.eventapis.api.Query;
import com.kloia.eventapis.cassandra.QueryByPersistentEventRepositoryDelegate;
import com.kloia.sample.model.Order;
import com.kloia.sample.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    PersistentEventRepository<Order> orderPersistentEventRepository(List<EntityFunctionSpec<Order, ?>> orderFunctionSpecs){
        CassandraSession cassandraSession = new CassandraSession(eventApisConfiguration.getStoreConfig());
        PersistentEventRepository<Order> cassandraEventRepository = new CassandraEventRepository<>(eventApisConfiguration.getTableNames().getOrDefault("orderevents", "orderevents"), cassandraSession, objectMapper);
        cassandraEventRepository.addCommandSpecs(orderFunctionSpecs);
        return cassandraEventRepository;
    }
    @Bean
    EventRepository<Order> orderEventRepository(PersistentEventRepository<Order> orderPersistentEventRepository, IOperationRepository operationRepository, IUserContext userContext){

        return new CompositeRepositoryImpl(orderPersistentEventRepository, operationContext, new ObjectMapper(), operationRepository, userContext);
    }
    @Bean
    IOperationRepository kafkaOperationRepository(){
        KafkaOperationRepositoryFactory kafka = new KafkaOperationRepositoryFactory(eventApisConfiguration.getEventBus());
       return kafka.createKafkaOperationRepository(objectMapper);
    }


    @Bean
    Query<Order> orderQuery(PersistentEventRepository<Order> orderPersistentEventRepository){
        return new QueryByPersistentEventRepositoryDelegate<>(orderPersistentEventRepository);
    }

/*    @Bean
    @Scope("prototype")
    public Feign.Builder feignBuilder() {
        return Feign.builder();
    }*/

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private OrderRepository orderRepository;


    @KafkaListener(id = "op-listener", topics = "operation-events", containerFactory = "operationsKafkaListenerContainerFactory")
    private void listenOperations(ConsumerRecord<String, Operation> data) throws EventStoreException {
        PersistentEventRepository<Order> eventRepository = applicationContext.getBean(PersistentEventRepository.class);
        log.warn("Incoming Message: " + data.value());
        if (data.value().getTransactionState() == TransactionState.TXN_FAILED) {
            eventRepository.markFail(data.key());
            Order order = eventRepository.queryEntity(data.key());
            if(order != null && order.getId() != null)
                orderRepository.save(order);
        }else if (data.value().getTransactionState() == TransactionState.TXN_SUCCEDEED) {
            eventRepository.queryByOpId(data.key()).stream().forEach(orderRepository::save);
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
