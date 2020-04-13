package com.kloia.eventapis.api.emon.service.processor;

import com.hazelcast.map.AbstractEntryProcessor;
import com.kloia.eventapis.api.emon.domain.Partition;
import com.kloia.eventapis.api.emon.domain.ServiceData;
import com.kloia.eventapis.api.emon.domain.Topic;

import java.util.Collections;
import java.util.Map;

public class ConsumerOffsetChangeEntryProcessor extends AbstractEntryProcessor<String, Topic> {

    private static final long serialVersionUID = -8499685451383136540L;

    private final String group;
    private final int partitionNo;
    private final long offset;

    public ConsumerOffsetChangeEntryProcessor(String group, int partitionNo, long offset) {
        this.group = group;
        this.partitionNo = partitionNo;
        this.offset = offset;
    }

    @Override
    public Partition process(Map.Entry<String, Topic> entry) {
        Topic topic = entry.getValue();
        if (topic == null) {
            Topic newTopic = Topic.createTopic(group, partitionNo, offset);
            entry.setValue(newTopic);
            return new Partition(partitionNo, 0L);
        }
        Map<String, ServiceData> serviceDataHashMap = topic.getServiceDataHashMap();
        ServiceData serviceData = serviceDataHashMap.get(group);
        if (serviceData == null) {
            serviceDataHashMap.put(group, ServiceData.createServiceData(group, Collections.singletonList(new Partition(partitionNo, offset))));
            entry.setValue(topic);
            return new Partition(partitionNo, 0L);
        }
        Partition partition = serviceData.getPartition(partitionNo);
        if (partition == null || partition.getOffset() < offset) {
            serviceData.setPartition(new Partition(partitionNo, offset));
            entry.setValue(topic);
            return partition != null ? partition : new Partition(partitionNo, 0L);
        }

        return null; // nothing changed
    }
}
