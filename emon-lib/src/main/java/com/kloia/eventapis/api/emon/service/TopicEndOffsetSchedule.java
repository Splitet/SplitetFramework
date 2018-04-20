package com.kloia.eventapis.api.emon.service;

import com.hazelcast.core.IMap;
import com.hazelcast.map.AbstractEntryProcessor;
import com.hazelcast.spring.context.SpringAware;
import com.kloia.eventapis.api.emon.domain.Partition;
import com.kloia.eventapis.api.emon.domain.Topic;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@SpringAware
@Component
@ConditionalOnProperty(value = "emon.offsetScheduler.enabled", havingValue = "true")
class TopicEndOffsetSchedule extends ScheduledTask {

    private transient Consumer kafkaConsumer;
    private transient IMap<String, Topic> topicsMap;

    public TopicEndOffsetSchedule() {
    }


    @Override
    boolean runInternal(StopWatch stopWatch) {

        stopWatch.start("collectEndOffsets");
        List<TopicPartition> collect = topicsMap.entrySet().stream().flatMap(
                topic -> topic.getValue().getPartitions().stream().map(partition -> new TopicPartition(topic.getKey(), partition.getNumber()))
        ).collect(Collectors.toList());
        java.util.Map<TopicPartition, Long> map = kafkaConsumer.endOffsets(collect);
        java.util.Map<String, List<Partition>> result = new HashMap<>();
        map.forEach((topicPartition, endOffset) -> {
            if (!result.containsKey(topicPartition.topic()))
                result.put(topicPartition.topic(),new ArrayList<>());
            result.get(topicPartition.topic()).add(new Partition(topicPartition.partition(),endOffset));
        });
        result.forEach((topic, endOffset) -> topicsMap.executeOnKey(topic, new EndOffsetSetter(endOffset)));
        log.debug("collectEndOffsets:" + result.toString());

        stopWatch.stop();
        log.debug("TopicEndOffsetSchedule:" + topicsMap.entrySet());
        log.debug(stopWatch.prettyPrint());
        return true;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    @Autowired
    public void setScheduleRateInMillis(@Value("${emon.offsetScheduler.schedulesInMillis.TopicEndOffsetSchedule:500}") Long scheduleRateInMillis) {
        this.scheduleRateInMillis = scheduleRateInMillis;
    }

    @Autowired
    public void setTopicsMap(IMap topicsMap) {
        this.topicsMap = topicsMap;
    }

    @Autowired
    public void setKafkaConsumer(Consumer kafkaConsumer) {
        this.kafkaConsumer = kafkaConsumer;
    }

    private static class EndOffsetSetter extends AbstractEntryProcessor<String, Topic> {
        private final List<Partition> partitions;

        public EndOffsetSetter(List<Partition> partitions) {
            this.partitions = partitions;
        }

        @Override
        public Object process(Map.Entry<String, Topic> entry) {
            Topic topic = entry.getValue();
            topic.setPartitions(partitions);
            entry.setValue(topic);
            return entry;
        }
    }
}
