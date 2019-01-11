package com.kloia.eventapis.spring.configuration;

import com.kloia.eventapis.common.PublishedEvent;
import com.kloia.eventapis.pojos.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.StopWatch;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Slf4j
@Configuration
public class AutomaticTopicConfiguration {
    public static final int DEFAULT_NUM_PARTITIONS = 3;
    public static final short DEFAULT_REPLICATION_FACTOR = 1;
    @Autowired
    private EventApisConfiguration eventApisConfiguration;

    private AdminClient adminClient() {
        Properties properties = new Properties();
        properties.putAll(eventApisConfiguration.getEventBus().buildCommonProperties());
        return AdminClient.create(properties);
    }

    @PostConstruct
    public void init() {
        AdminClient adminClient = adminClient();
        try {
            StopWatch stopWatch = new StopWatch("CheckAndCreateTopics");
            stopWatch.start("CheckAndCreateTopics");
            ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
            provider.addIncludeFilter(new AssignableTypeFilter(PublishedEvent.class));
            Set<BeanDefinition> candidateComponents = provider.findCandidateComponents(eventApisConfiguration.getBaseEventsPackage());
            int numPartitions = getDefaultNumberOfPartitions(adminClient);
            for (BeanDefinition candidateComponent : candidateComponents) {
                Class<PublishedEvent> beanClass;
                try {
                    beanClass = (Class<PublishedEvent>) Class.forName(candidateComponent.getBeanClassName());
                    String topicName = beanClass.getSimpleName();
                    log.info("Candidate {} to Create Topic:", topicName);
                    try {
                        adminClient.describeTopics(Collections.singleton(topicName)).all().get();
                    } catch (UnknownTopicOrPartitionException | ExecutionException exception) {
                        if (!(exception.getCause() instanceof UnknownTopicOrPartitionException))
                            throw exception;
                        log.warn("Topic {} does not exists, trying to create", topicName);
                        try {
                            adminClient.createTopics(Collections.singleton(new NewTopic(topicName, numPartitions, DEFAULT_REPLICATION_FACTOR)));
                            log.info("Topic {} is Created Successfully:", topicName);
                        } catch (Exception topicCreationEx) {
                            log.warn("Error while creating Topic:" + topicCreationEx.getMessage(), topicCreationEx);
                        }
                    }
                } catch (ClassNotFoundException | InterruptedException | ExecutionException exception) {
                    log.warn("Error while checking Topic:" + candidateComponent.toString() + " message: " + exception.getMessage(), exception);
                }
            }
            stopWatch.stop();
            log.debug(stopWatch.prettyPrint());
        } finally {
            adminClient.close();
        }
    }

    private int getDefaultNumberOfPartitions(AdminClient adminClient) {
        try {
            return adminClient.describeTopics(Collections.singleton(Operation.OPERATION_EVENTS))
                    .all().get().values().iterator().next()
                    .partitions().size();
        } catch (InterruptedException | ExecutionException | NullPointerException e) {
            log.warn("Error while Calculating Number of Partitions from Topic: " + Operation.OPERATION_EVENTS + " Assuming " + DEFAULT_NUM_PARTITIONS);
            return DEFAULT_NUM_PARTITIONS;
        }
    }
}


