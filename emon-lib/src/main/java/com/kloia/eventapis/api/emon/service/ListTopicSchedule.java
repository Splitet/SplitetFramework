package com.kloia.eventapis.api.emon.service;

import com.hazelcast.core.IMap;
import com.hazelcast.scheduledexecutor.NamedTask;
import com.hazelcast.spring.context.SpringAware;
import com.kloia.eventapis.api.emon.domain.Topic;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.TopicPartitionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@SpringAware
@Component
class ListTopicSchedule implements Runnable, NamedTask, Serializable {

    private transient AdminClient adminClient;
    private transient IMap<String, Topic> topicsMap;

    public ListTopicSchedule() {
    }

    public ListTopicSchedule(AdminClient adminClient, IMap<String, Topic> topicsMap) {
        this.adminClient = adminClient;
        this.topicsMap = topicsMap;
    }

    @Override
    public void run() {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("adminClient.listTopics()");
        try {
            Collection<String> topicNames = adminClient.listTopics().listings().get()
                    .stream().map(TopicListing::name).collect(Collectors.toList());
            topicNames.forEach(topicName -> topicsMap.putIfAbsent(topicName, new Topic()));

            topicsMap.removeAll(mapEntry -> topicNames.stream().anyMatch(topicName -> Objects.equals(topicName, mapEntry.getKey())));

            DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(topicsMap.keySet());
            describeTopicsResult.all().get().forEach(
                    (topic, topicDescription) -> topicsMap.get(topic).setPartitions(
                            topicDescription.partitions().stream().map(TopicPartitionInfo::partition).collect(Collectors.toList()))
            );
        } catch (InterruptedException | ExecutionException e) {
            log.warn("Error While trying to fetch Topic List " + e.getMessage(), e);
        }
        stopWatch.stop();
        log.info("Topics:" + topicsMap.entrySet());
        log.info(stopWatch.prettyPrint());
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Autowired
    public void setTopicsMap(IMap topicsMap) {
        this.topicsMap = topicsMap;
    }

    @Autowired
    public void setAdminClient(AdminClient adminClient) {
        this.adminClient = adminClient;
    }

/*    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }*/
}
