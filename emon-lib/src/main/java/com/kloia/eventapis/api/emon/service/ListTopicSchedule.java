package com.kloia.eventapis.api.emon.service;

import com.hazelcast.core.IMap;
import com.hazelcast.map.AbstractEntryProcessor;
import com.hazelcast.query.Predicate;
import com.hazelcast.spring.context.SpringAware;
import com.kloia.eventapis.api.emon.domain.Partition;
import com.kloia.eventapis.api.emon.domain.Topic;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.TopicPartitionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@SpringAware
@Component
@ConditionalOnProperty(value = "emon.offsetScheduler.enabled", havingValue = "true")
public class ListTopicSchedule extends ScheduledTask {

    private transient AdminClient adminClient;
    private transient IMap<String, Topic> topicsMap;
    private transient Pattern eventTopicRegex;

    public ListTopicSchedule() {
    }

    public ListTopicSchedule(AdminClient adminClient, IMap<String, Topic> topicsMap) {
        this.adminClient = adminClient;
        this.topicsMap = topicsMap;
    }

    @Override
    boolean runInternal(StopWatch stopWatch) throws InterruptedException, ExecutionException {

        stopWatch.start("adminClient.listTopics()");
        Collection<String> topicNames = adminClient.listTopics().listings().get()
                .stream().map(TopicListing::name).filter(this::shouldCollectEvent).collect(Collectors.toList());
        topicsMap.removeAll(new RemoveTopicPredicate(topicNames));

        DescribeTopicsResult describeTopicsResult = adminClient.describeTopics(topicNames);
        describeTopicsResult.all().get().forEach(
                (topic, topicDescription) -> topicsMap.executeOnKey(topic, new SetTopicPartitionsProcessor(
                        topicDescription.partitions().stream().map(TopicPartitionInfo::partition).collect(Collectors.toList()))
                )
        );
        metaMap.set(this.getName() + TopicServiceScheduler.LAST_SUCCESS_PREFIX, System.currentTimeMillis());
        log.debug("Topics:" + topicsMap.entrySet());
        log.debug(stopWatch.prettyPrint());
        return true;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    @Autowired
    public void setScheduleRateInMillis(@Value("${emon.offsetScheduler.schedulesInMillis.ListTopicSchedule:10000}") Long scheduleRateInMillis) {
        this.scheduleRateInMillis = scheduleRateInMillis;
    }


    @Autowired
    public void setTopicsMap(IMap topicsMap) {
        this.topicsMap = topicsMap;
    }

    @Autowired
    public void setAdminClient(AdminClient adminClient) {
        this.adminClient = adminClient;
    }

    @Autowired
    public void setEventTopicRegex(@Qualifier("eventTopicRegex") Pattern eventTopicRegex) {
        this.eventTopicRegex = eventTopicRegex;
    }

    private boolean shouldCollectEvent(String topic) {
        return topic != null && (eventTopicRegex.matcher(topic).matches() || topic.equals("operation-events"));
    }

    private static class SetTopicPartitionsProcessor extends AbstractEntryProcessor<String, Topic> {
        private final List<Integer> partitions;

        private SetTopicPartitionsProcessor(List<Integer> partitions) {
            this.partitions = partitions;
        }

        @Override
        public Object process(Map.Entry<String, Topic> entry) {
            final Topic topic = entry.getValue() == null ? new Topic() : entry.getValue();
            partitions.forEach(
                    partitionNo -> topic.getPartitions()
                            .putIfAbsent(partitionNo, new Partition(partitionNo)));
            entry.setValue(topic);
            return entry;
        }
    }

    private static class RemoveTopicPredicate implements Predicate<String, Topic> {
        private final Collection<String> topicNames;

        public RemoveTopicPredicate(Collection<String> topicNames) {
            this.topicNames = topicNames;
        }

        @Override
        public boolean apply(Map.Entry<String, Topic> mapEntry) {
            return topicNames.stream().noneMatch(topicName -> Objects.equals(topicName, mapEntry.getKey()));
        }
    }
}
