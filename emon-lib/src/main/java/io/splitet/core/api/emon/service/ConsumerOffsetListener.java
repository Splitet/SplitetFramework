package io.splitet.core.api.emon.service;

import com.hazelcast.core.IMap;
import io.splitet.core.api.emon.domain.Partition;
import io.splitet.core.api.emon.domain.Topic;
import io.splitet.core.api.emon.service.processor.ConsumerOffsetChangeEntryProcessor;
import io.splitet.core.pojos.Operation;
import kafka.common.OffsetAndMetadata;
import kafka.coordinator.group.BaseKey;
import kafka.coordinator.group.GroupMetadataManager;
import kafka.coordinator.group.GroupTopicPartition;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ConsumerOffsetListener implements MessageListener<byte[], byte[]>, ConsumerSeekAware {


    private transient Pattern eventTopicRegex;
    private transient Pattern consumerGroupRegex;

    @Autowired
    private IMap<String, Topic> topicsMap;

    public void onMessage(ConsumerRecord<byte[], byte[]> data) {
        BaseKey baseKey = GroupMetadataManager.readMessageKey(ByteBuffer.wrap(data.key()));
        if (!(baseKey.key() instanceof GroupTopicPartition)) {
            log.warn("Type of Base Key: {} Key: {}", baseKey.key() != null ? baseKey.key().getClass().getName() : null, baseKey.key());
            return;
        }
        GroupTopicPartition key = (GroupTopicPartition) baseKey.key();
        if (data.value() == null || data.value().length == 0) {
            log.warn("Value is null or Empty for: {}", key.toString());
            return;
        }
        OffsetAndMetadata offsetAndMetadata = GroupMetadataManager.readOffsetMessageValue(ByteBuffer.wrap(data.value()));

        String group = key.group();
        int partitionNo = key.topicPartition().partition();
        String topic = key.topicPartition().topic();
        long offset = offsetAndMetadata.offset();
        if (!(shouldCollectEvent(topic) && shouldCollectConsumer(group))) {
            return;
        }

        Partition oldPartition = (Partition) topicsMap.executeOnKey(topic, new ConsumerOffsetChangeEntryProcessor(group, partitionNo, offset));

        if (oldPartition != null) {
            log.info("Changed Old Partition: {} {} {} newOffset: {}", topic, group, oldPartition.toString(), offset);
        }

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
        return topic != null && (eventTopicRegex.matcher(topic).matches() || Operation.OPERATION_EVENTS.equals(topic));
    }

    private boolean shouldCollectConsumer(String consumer) {
        return consumerGroupRegex.matcher(consumer).matches();
    }

    @Override
    public void registerSeekCallback(ConsumerSeekAware.ConsumerSeekCallback callback) {

    }

    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekAware.ConsumerSeekCallback callback) {
        assignments.keySet().forEach(topicPartition -> callback.seekToEnd(topicPartition.topic(), topicPartition.partition()));

    }

    @Override
    public void onIdleContainer(Map<TopicPartition, Long> assignments, ConsumerSeekAware.ConsumerSeekCallback callback) {

    }

}
