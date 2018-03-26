package com.kloia.eventapis.spring.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.api.IUserContext;
import com.kloia.eventapis.api.impl.EmptyUserContext;
import com.kloia.eventapis.cassandra.CassandraSession;
import com.kloia.eventapis.common.CommandExecutionInterceptor;
import com.kloia.eventapis.common.EventExecutionInterceptor;
import com.kloia.eventapis.common.OperationContext;
import com.kloia.eventapis.kafka.KafkaOperationRepository;
import com.kloia.eventapis.kafka.KafkaOperationRepositoryFactory;
import com.kloia.eventapis.kafka.KafkaProperties;
import com.kloia.eventapis.kafka.PublishedEventWrapper;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.spring.filter.OpContextFilter;
import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.config.AbstractKafkaListenerContainerFactory;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpoint;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.kafka.support.TopicPartitionInitialOffset;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.PreDestroy;
import javax.servlet.DispatcherType;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;

@Slf4j
@Configuration
@Import(SpringKafkaOpListener.class)
public class EventApisFactory {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private EventApisConfiguration eventApisConfiguration;

    @Autowired
    private CassandraSession cassandraSession;

    @Bean
    public OperationContext createOperationContext() {
        return new OperationContext();
    }

    @Bean
    CassandraSession cassandraSession() {
        return new CassandraSession(eventApisConfiguration.getStoreConfig());
    }

    @PreDestroy
    public void destroy() {
        cassandraSession.destroy();
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
    public EventMessageConverter eventMessageConverter(OperationContext operationContext, IUserContext userContext) {
        return new EventMessageConverter(objectMapper, operationContext, userContext);
    }

    @Bean
    public ConsumerFactory<String, PublishedEventWrapper> kafkaConsumerFactory(KafkaOperationRepositoryFactory kafkaOperationRepositoryFactory) {
        return new EventApisConsumerFactory<String, PublishedEventWrapper>(eventApisConfiguration, true) {
            @Override
            public Consumer<String, PublishedEventWrapper> createConsumer() {
                return kafkaOperationRepositoryFactory.createEventConsumer(objectMapper);
            }
        };
    }

    @Bean
    public ConsumerFactory<String, Operation> kafkaOperationsFactory(KafkaOperationRepositoryFactory kafkaOperationRepositoryFactory) {
        return new EventApisConsumerFactory<String, Operation>(eventApisConfiguration, false) {
            @Override
            public Consumer<String, Operation> createConsumer() {
                return kafkaOperationRepositoryFactory.createOperationConsumer(objectMapper);
            }
        };
    }

    @Bean({"eventsKafkaListenerContainerFactory", "kafkaListenerContainerFactory"})
    public ConcurrentKafkaListenerContainerFactory<String, PublishedEventWrapper> eventsKafkaListenerContainerFactory(
            EventMessageConverter eventMessageConverter, ConsumerFactory<String, PublishedEventWrapper> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, PublishedEventWrapper> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(eventApisConfiguration.getEventBus().getConsumer().getEventConcurrency());
        factory.setMessageConverter(eventMessageConverter);
        factory.getContainerProperties().setPollTimeout(3000);
        factory.getContainerProperties().setAckMode(AbstractMessageListenerContainer.AckMode.RECORD);
        return factory;
    }

    @Bean("operationsKafkaListenerContainerFactory")
    public KafkaListenerContainerFactory<KafkaMessageListenerContainer<String, Operation>> operationsKafkaListenerContainerFactory(
            ConsumerFactory<String, Operation> consumerFactory,
            PlatformTransactionManager platformTransactionManager) {
        AbstractKafkaListenerContainerFactory<KafkaMessageListenerContainer<String, Operation>, String, Operation> abstractKafkaListenerContainerFactory
                = new EventApisKafkaListenerContainerFactory(consumerFactory);
        RetryTemplate retryTemplate = new RetryTemplate();
        abstractKafkaListenerContainerFactory.setRetryTemplate(retryTemplate);
        abstractKafkaListenerContainerFactory.getContainerProperties().setPollTimeout(3000L);
        abstractKafkaListenerContainerFactory.getContainerProperties().setAckOnError(false);
        abstractKafkaListenerContainerFactory.getContainerProperties().setAckMode(AbstractMessageListenerContainer.AckMode.RECORD);
        abstractKafkaListenerContainerFactory.getContainerProperties().setTransactionManager(platformTransactionManager);
        return abstractKafkaListenerContainerFactory;
    }

    @Bean
    @ConditionalOnMissingBean(IUserContext.class)
    public IUserContext getUserContext() {
        return new EmptyUserContext();
    }

    public static class EventApisKafkaListenerContainerFactory extends AbstractKafkaListenerContainerFactory<KafkaMessageListenerContainer<String, Operation>, String, Operation> {
        private final ConsumerFactory<String, Operation> consumerFactory;

        public EventApisKafkaListenerContainerFactory(ConsumerFactory<String, Operation> consumerFactory) {
            this.consumerFactory = consumerFactory;
        }

        @Override
        protected KafkaMessageListenerContainer<String, Operation> createContainerInstance(KafkaListenerEndpoint endpoint) {
            ContainerProperties containerProperties;
            Collection<TopicPartitionInitialOffset> topicPartitions = endpoint.getTopicPartitions();
            if (!topicPartitions.isEmpty()) {
                containerProperties = new ContainerProperties(
                        topicPartitions.toArray(new TopicPartitionInitialOffset[topicPartitions.size()]));
            } else {
                Collection<String> topics = endpoint.getTopics();
                if (!topics.isEmpty()) {
                    containerProperties = new ContainerProperties(topics.toArray(new String[topics.size()]));
                } else {
                    containerProperties = new ContainerProperties(endpoint.getTopicPattern());
                }
            }
            return new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        }

        @Override
        protected void initializeContainer(KafkaMessageListenerContainer<String, Operation> instance) {
//                super.initializeContainer(instance);
        }
    }

    public abstract static class EventApisConsumerFactory<K, V> implements ConsumerFactory<K, V> {
        private final EventApisConfiguration eventApisConfiguration;
        private final boolean autoCommit;

        public EventApisConsumerFactory(EventApisConfiguration eventApisConfiguration, boolean autoCommit) {
            this.autoCommit = autoCommit;
            this.eventApisConfiguration = eventApisConfiguration;
        }

        @Override
        public Consumer<K, V> createConsumer(String clientIdSuffix) {
            return createConsumer();
        }

        @Override
        public Consumer<K, V> createConsumer(String groupId, String clientIdSuffix) {
            return createConsumer();
        }

        @Override
        public boolean isAutoCommit() {
            return autoCommit;
        }

        @Override
        public Map<String, Object> getConfigurationProperties() {
            return eventApisConfiguration.getEventBus().buildConsumerProperties();

        }
    }
}
