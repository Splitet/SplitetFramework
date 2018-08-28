package com.kloia.eventapis.api.emon.service;

import com.hazelcast.core.IMap;
import com.hazelcast.map.AbstractEntryProcessor;
import com.hazelcast.spring.context.SpringAware;
import com.kloia.eventapis.api.emon.domain.Partition;
import com.kloia.eventapis.api.emon.domain.ServiceData;
import com.kloia.eventapis.api.emon.domain.Topic;
import kafka.coordinator.group.GroupOverview;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import scala.collection.JavaConversions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@SpringAware
@Component
@ConditionalOnProperty(value = "emon.offsetScheduler.enabled", havingValue = "true")
public class ConsumerOffsetSchedule extends ScheduledTask {

    private transient kafka.admin.AdminClient adminToolsClient;
    private transient IMap<String, Topic> topicsMap;
    private transient Pattern eventTopicRegex;
    private transient Pattern consumerGroupRegex;


    @Override
    public boolean runInternal(StopWatch stopWatch) {
        AtomicBoolean isSuccess = new AtomicBoolean(true);

        stopWatch.start("ConsumerOffsetSchedule.listAllConsumerGroupsFlattened()");
        List<String> groupList = JavaConversions.seqAsJavaList(adminToolsClient.listAllConsumerGroupsFlattened())
                .stream().map(GroupOverview::groupId).collect(Collectors.toList());

        log.debug("consumerGroups: " + groupList.toString());
        stopWatch.stop();

        Map<String, Topic> topicMap = topicsMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, o -> new Topic(new HashMap<>(), o.getValue().getPartitions())));

        stopWatch.start("ConsumerOffsetSchedule.collectGroupOffsets");
        groupList.forEach(consumer -> {
            if (shouldCollectConsumer(consumer)) {
                try {
                    scala.collection.immutable.Map<TopicPartition, Object> listGroupOffsets = adminToolsClient.listGroupOffsets(consumer);
                    java.util.Map<TopicPartition, Object> map = JavaConversions.mapAsJavaMap(listGroupOffsets);
                    java.util.Map<String, List<Partition>> result = map.entrySet().stream().collect(
                            Collectors.toMap(
                                    entry -> entry.getKey().topic(),
                                    entry ->
                                            Collections.singletonList(
                                                    new Partition(entry.getKey().partition(), (Long) entry.getValue())
                                            ),
                                    (u1, u2) -> Stream.concat(u1.stream(), u2.stream()).collect(Collectors.toList())));
                    for (java.util.Map.Entry<String, List<Partition>> entry : result.entrySet()) {
                        if (shouldCollectEvent(entry.getKey())) {
//                            topicsMap.executeOnKey(entry.getKey(), new ConsumerOffsetProcessor(consumer, entry.getValue()));
                            topicMap.compute(entry.getKey(), (s, topic) -> {
                                if (topic == null)
                                    topic = new Topic();
                                ServiceData serviceData = topic.getServiceDataHashMap().get(consumer);
                                if (serviceData == null) {
                                    serviceData = ServiceData.createServiceData(consumer, entry.getValue());
                                    topic.getServiceDataHashMap().put(consumer, serviceData);
                                } else
                                    serviceData.setPartitions(entry.getValue());
                                return topic;
                            });

                        }
                    }
                    log.debug(listGroupOffsets.toString());
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    isSuccess.set(false);
                }
            }
        });
        topicMap.forEach((s, topic) -> topicsMap.executeOnKey(s, new ConsumerOffsetProcessor(topic.getServiceDataHashMap())));
        stopWatch.stop();
        log.debug("Topics:" + topicsMap.entrySet());
        return isSuccess.get();
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    @Autowired
    public void setScheduleRateInMillis(@Value("${emon.offsetScheduler.schedulesInMillis.ConsumerOffsetSchedule:500}") Long scheduleRateInMillis) {
        this.scheduleRateInMillis = scheduleRateInMillis;
    }

    @Autowired
    public void setTopicsMap(IMap topicsMap) {
        this.topicsMap = topicsMap;
    }

    @Autowired
    public void setAdminToolsClient(kafka.admin.AdminClient adminToolsClient) {
        this.adminToolsClient = adminToolsClient;
    }

    @Autowired
    public void setEventTopicRegex(Pattern eventTopicRegex) {
        this.eventTopicRegex = eventTopicRegex;
    }

    @Autowired
    public void setConsumerGroupRegex(Pattern consumerGroupRegex) {
        this.consumerGroupRegex = consumerGroupRegex;
    }

    private boolean shouldCollectEvent(String topic) {
        return topic != null && (eventTopicRegex.matcher(topic).matches() || topic.equals("operation-events"));
    }

    private boolean shouldCollectConsumer(String consumer) {
        return consumerGroupRegex.matcher(consumer).matches();
    }

    private static class ConsumerOffsetProcessor extends AbstractEntryProcessor<String, Topic> {
        private Map<String, ServiceData> serviceDataMap;

        public ConsumerOffsetProcessor(Map<String, ServiceData> serviceDataMap) {
            this.serviceDataMap = serviceDataMap;
        }

        @Override
        public Object process(Map.Entry<String, Topic> entry) {
            Topic topic = entry.getValue();
            if (topic == null)
                topic = new Topic();
            topic.setServiceDataHashMap(serviceDataMap);
            entry.setValue(topic);
            return entry;
        }
    }
}
