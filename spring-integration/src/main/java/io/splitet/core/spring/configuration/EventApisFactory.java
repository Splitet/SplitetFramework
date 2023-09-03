package io.splitet.core.spring.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import feign.RequestInterceptor;
import io.splitet.core.api.IUserContext;
import io.splitet.core.api.impl.EmptyUserContext;
import io.splitet.core.cassandra.CassandraSession;
import io.splitet.core.common.CommandExecutionInterceptor;
import io.splitet.core.common.EventExecutionInterceptor;
import io.splitet.core.common.OperationContext;
import io.splitet.core.kafka.JsonDeserializer;
import io.splitet.core.kafka.KafkaOperationRepository;
import io.splitet.core.kafka.KafkaOperationRepositoryFactory;
import io.splitet.core.kafka.KafkaProperties;
import io.splitet.core.kafka.PublishedEventWrapper;
import io.splitet.core.pojos.Operation;
import io.splitet.core.spring.filter.OpContextFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

@Slf4j
@Configuration
@Import(SpringKafkaOpListener.class)
public class EventApisFactory {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private EventApisConfiguration eventApisConfiguration;

    @Bean
    public OperationContext createOperationContext() {
        return new OperationContext();
    }

    @Bean
    CassandraSession cassandraSession() {
        return new CassandraSession(eventApisConfiguration.getStoreConfig());
    }

    @Bean
    public FilterRegistrationBean createOpContextFilter(@Autowired OperationContext operationContext) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new OpContextFilter(operationContext));
//        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
        return registration;
    }

    @Bean
    @Scope("prototype")
    public RequestInterceptor opIdInterceptor(@Autowired OperationContext operationContext) {
        return template -> {
            String key = operationContext.getContextOpId();
            if (key != null) {
                template.header(OpContextFilter.OP_ID_HEADER, key);
//                template.header(OperationContext.OP_ID, key); // legacy
            }
        };
    }

    @Bean
    public CommandExecutionInterceptor createCommandExecutionInterceptor(@Autowired KafkaOperationRepository kafkaOperationRepository,
                                                                         @Autowired OperationContext operationContext) {
        return new CommandExecutionInterceptor(kafkaOperationRepository, operationContext);
    }

    @Bean
    public EventExecutionInterceptor createEventExecutionInterceptor(@Autowired KafkaOperationRepository kafkaOperationRepository,
                                                                     @Autowired OperationContext operationContext,
                                                                     @Autowired IUserContext userContext) {
        return new EventExecutionInterceptor(kafkaOperationRepository, operationContext, userContext);
    }

    @Bean
    public KafkaOperationRepositoryFactory kafkaOperationRepositoryFactory(@Autowired OperationContext operationContext,
                                                                           IUserContext userContext) {
        KafkaProperties eventBus = eventApisConfiguration.getEventBus();
        return new KafkaOperationRepositoryFactory(eventBus, userContext, operationContext);
    }

    @Bean
    public KafkaOperationRepository kafkaOperationRepository(KafkaOperationRepositoryFactory kafkaOperationRepositoryFactory) {
        return kafkaOperationRepositoryFactory.createKafkaOperationRepository(objectMapper);
    }

    @Bean
    public EventMessageConverter eventMessageConverter(OperationContext operationContext, IUserContext userContext, KafkaOperationRepository kafkaOperationRepository) {
        return new EventMessageConverter(objectMapper, operationContext, userContext, kafkaOperationRepository);
    }

    @Bean
    public ConsumerFactory<String, PublishedEventWrapper> kafkaConsumerFactory() {
        KafkaProperties properties = eventApisConfiguration.getEventBus().clone();
        properties.getConsumer().setEnableAutoCommit(false);
        return new DefaultKafkaConsumerFactory<>(properties.buildConsumerProperties(),
                new StringDeserializer(), new JsonDeserializer<>(PublishedEventWrapper.class, objectMapper));
    }

    @Bean
    public ConsumerFactory<String, Operation> kafkaOperationsFactory() {
        KafkaProperties properties = eventApisConfiguration.getEventBus().clone();
        properties.getConsumer().setEnableAutoCommit(false);
        return new DefaultKafkaConsumerFactory<>(properties.buildConsumerProperties(),
                new StringDeserializer(), new JsonDeserializer<>(Operation.class, objectMapper));
    }

    @Bean({"eventsKafkaListenerContainerFactory", "kafkaListenerContainerFactory"})
    public ConcurrentKafkaListenerContainerFactory<String, PublishedEventWrapper> eventsKafkaListenerContainerFactory(
            EventMessageConverter eventMessageConverter, ConsumerFactory<String, PublishedEventWrapper> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, PublishedEventWrapper> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(eventApisConfiguration.getEventBus().getConsumer().getEventConcurrency());
        factory.setMessageConverter(eventMessageConverter);
        factory.getContainerProperties().setPollTimeout(3000);
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(eventApisConfiguration.getEventBus().getConsumer().getEventSchedulerPoolSize());
        scheduler.setBeanName("EventsFactory-Scheduler");
        scheduler.initialize();

        factory.getContainerProperties().setScheduler(scheduler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
    }

    @Bean("operationsKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Operation> operationsKafkaListenerContainerFactory(
            ConsumerFactory<String, Operation> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Operation> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        RetryTemplate retryTemplate = new RetryTemplate();
        factory.setRetryTemplate(retryTemplate);

        factory.setConcurrency(eventApisConfiguration.getEventBus().getConsumer().getOperationSchedulerPoolSize());
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(eventApisConfiguration.getEventBus().getConsumer().getOperationSchedulerPoolSize());
        scheduler.setBeanName("OperationsFactory-Scheduler");
        scheduler.initialize();
        factory.getContainerProperties().setScheduler(scheduler);
        ThreadPoolTaskScheduler consumerScheduler = new ThreadPoolTaskScheduler();
        consumerScheduler.setPoolSize(eventApisConfiguration.getEventBus().getConsumer().getOperationSchedulerPoolSize());
        consumerScheduler.setBeanName("OperationsFactory-ConsumerScheduler");
        consumerScheduler.initialize();

        factory.getContainerProperties().setPollTimeout(3000L);
        factory.getContainerProperties().setAckOnError(false);
        factory.getContainerProperties().setConsumerTaskExecutor(consumerScheduler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        /**
         * This is Fix for Spring Kafka versions which does not have ConsumerAwareErrorHandler handler till 2.0
         * When Listener faces with error, it retries snapshot operation
         * See https://github.com/kloiasoft/eventapis/issues/44
         */
        factory.getContainerProperties().setTransactionManager(new EmptyTransactionManager());
//        factory.getContainerProperties().setTransactionManager(platformTransactionManager);
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean(IUserContext.class)
    public IUserContext getUserContext() {
        return new EmptyUserContext();
    }

    private static class EmptyTransactionManager implements PlatformTransactionManager {
        @Override
        @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
        public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
            return null;
        }

        @Override
        public void commit(TransactionStatus status) throws TransactionException {

        }

        @Override
        public void rollback(TransactionStatus status) throws TransactionException {

        }
    }

}
