package io.splitet.core.api.emon.service.processor;

import com.hazelcast.map.AbstractEntryProcessor;
import io.splitet.core.api.emon.domain.Partition;
import io.splitet.core.api.emon.domain.Topic;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class EndOffsetSetter extends AbstractEntryProcessor<String, Topic> {

    private static final long serialVersionUID = 1754073597874826572L;

    private final int partitionNo;
    private final long offset;

    public EndOffsetSetter(int partitionNo, long offset) {
        this.partitionNo = partitionNo;
        this.offset = offset;
    }

    @Override
    public Object process(Map.Entry<String, Topic> entry) {
        Topic topic = entry.getValue();
        if (topic == null) {
            log.warn("Null Topic Registration in EndOffsetSetter" + entry.getKey());
            topic = new Topic();
        }
        topic.getPartitions().compute(partitionNo, (integer, partition) -> {
            if (partition == null) {
                return new Partition(partitionNo, offset);
            } else {
                partition.setOffset(Math.max(offset, partition.getOffset()));
            }
            return partition;
        });
        entry.setValue(topic);
        return entry;
    }
}
