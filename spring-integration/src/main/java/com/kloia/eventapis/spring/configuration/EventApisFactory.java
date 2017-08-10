package com.kloia.eventapis.spring.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.spring.filter.OpContextFilter;

import com.kloia.eventapis.common.OperationContext;
import com.kloia.eventapis.kafka.KafkaOperationRepository;
import com.kloia.eventapis.kafka.KafkaOperationRepositoryFactory;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.kafka.PublishedEventWrapper;
import com.kloia.eventapis.common.CommandExecutionInterceptor;
import com.kloia.eventapis.api.IUserContext;
import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

@Slf4j
@Configuration
public class EventApisFactory {

    @Autowired
    EventApisConfiguration eventApisConfiguration;
    @Autowired
    ObjectMapper objectMapper;

    @Bean
    public OperationContext createOperationContext() {
        return new OperationContext();
    }


    @Bean
    public FilterRegistrationBean createOpContextFilter(@Autowired OperationContext operationContext) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new OpContextFilter(operationContext));
        registration.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
        return registration;
    }

    @Bean
    @Scope("prototype")
    public RequestInterceptor opIdInterceptor(@Autowired OperationContext operationContext) {
        return template -> {
            String key = operationContext.getContext();
            if (key != null)
                template.header("opId", key.toString());
        };
    }

    @Bean
    public CommandExecutionInterceptor createCommandExecutionInterceptor(@Autowired KafkaOperationRepository kafkaOperationRepository,
                                                                         @Autowired OperationContext operationContext) {
        return new CommandExecutionInterceptor(kafkaOperationRepository, operationContext);
    }

    @Bean
    public KafkaOperationRepositoryFactory kafkaOperationRepositoryFactory() {
        return new KafkaOperationRepositoryFactory(eventApisConfiguration.getEventBus());
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
        return new ConsumerFactory<String, PublishedEventWrapper>() {
            @Override
            public Consumer<String, PublishedEventWrapper> createConsumer() {
                return kafkaOperationRepositoryFactory.createEventConsumer(objectMapper);
            }

            @Override
            public boolean isAutoCommit() {
                return kafkaOperationRepositoryFactory.isAutoCommit();
            }
        };
    }

    @Bean
    public ConsumerFactory<String,Operation> kafkaOperationsFactory(KafkaOperationRepositoryFactory kafkaOperationRepositoryFactory) {
        return new ConsumerFactory<String, Operation>() {
            @Override
            public Consumer<String, Operation> createConsumer() {
                return kafkaOperationRepositoryFactory.createOperationConsumer(objectMapper);
            }

            @Override
            public boolean isAutoCommit() {
                return kafkaOperationRepositoryFactory.isAutoCommit();
            }
        };
    }

    @Bean({"eventsKafkaListenerContainerFactory", "kafkaListenerContainerFactory"})
    public ConcurrentKafkaListenerContainerFactory<String, PublishedEventWrapper> eventsKafkaListenerContainerFactory(
            EventMessageConverter eventMessageConverter, ConsumerFactory<String,PublishedEventWrapper> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, PublishedEventWrapper> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(3);
        factory.setMessageConverter(eventMessageConverter);
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }

    @Bean("operationsKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Operation> operationsKafkaListenerContainerFactory(ConsumerFactory<String,Operation> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Operation> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(3);
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }


}
