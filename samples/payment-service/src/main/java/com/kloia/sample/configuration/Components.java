package com.kloia.sample.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.api.impl.IOperationRepository;
import com.kloia.eventapis.api.impl.KafkaOperationRepositoryFactory;
import com.kloia.eventapis.api.impl.OperationContext;
import com.kloia.eventapis.configuration.EventApisConfiguration;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.pojos.TransactionState;
import com.kloia.evented.CassandraEventRepository;
import com.kloia.evented.CassandraSession;
import com.kloia.evented.EntityFunctionSpec;
import com.kloia.evented.EventRepository;
import com.kloia.evented.EventRepositoryImpl;
import com.kloia.evented.IEventRepository;
import com.kloia.evented.IUserContext;
import com.kloia.evented.Query;
import com.kloia.evented.QueryImpl;
import com.kloia.sample.model.Payment;
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
    IEventRepository<Payment> orderRepository(List<EntityFunctionSpec<Payment, ?>> orderFunctionSpecs){
        CassandraSession cassandraSession = new CassandraSession(eventApisConfiguration.getStoreConfig());
        CassandraEventRepository<Payment> cassandraEventRepository = new CassandraEventRepository<>(eventApisConfiguration.getTableNames().getOrDefault("paymentevents", "orderevents"), cassandraSession, objectMapper);
        cassandraEventRepository.addCommandSpecs(orderFunctionSpecs);
        return cassandraEventRepository;
    }
    @Bean
    EventRepository<Payment> orderEventRepository(IEventRepository<Payment> orderIEventRepository,IOperationRepository operationRepository,IUserContext userContext){

        return new EventRepositoryImpl(orderIEventRepository, operationContext, new ObjectMapper(), operationRepository, userContext);
    }
    @Bean
    IOperationRepository kafkaOperationRepository(){
        KafkaOperationRepositoryFactory kafka = new KafkaOperationRepositoryFactory(eventApisConfiguration.getEventBus());
       return kafka.createKafkaOperationRepository(objectMapper);
    }


    @Bean
    Query<Payment> orderQuery(IEventRepository<Payment> orderIEventRepository){
        return new QueryImpl<>(orderIEventRepository);
    }

/*    @Bean
    @Scope("prototype")
    public Feign.Builder feignBuilder() {
        return Feign.builder();
    }*/

    @Autowired
    private ApplicationContext applicationContext;


    @KafkaListener(id = "op-listener", topics = "operation-events", containerFactory = "operationsKafkaListenerContainerFactory")
    private void listenOperations(ConsumerRecord<String, Operation> data) {
        IEventRepository eventRepository = applicationContext.getBean(IEventRepository.class);
        log.warn("Incoming Message: " + data.value());
        if (data.value().getTransactionState() == TransactionState.TXN_FAILED) {
            eventRepository.markFail(data.key());
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
