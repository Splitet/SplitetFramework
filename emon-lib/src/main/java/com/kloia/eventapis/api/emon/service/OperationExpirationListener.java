package com.kloia.eventapis.api.emon.service;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import com.hazelcast.map.listener.EntryExpiredListener;
import com.kloia.eventapis.api.emon.domain.HandledEvent;
import com.kloia.eventapis.api.emon.domain.NoneHandled;
import com.kloia.eventapis.api.emon.domain.Partition;
import com.kloia.eventapis.api.emon.domain.ProducedEvent;
import com.kloia.eventapis.api.emon.domain.ServiceData;
import com.kloia.eventapis.api.emon.domain.Topic;
import com.kloia.eventapis.api.emon.domain.Topology;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class OperationExpirationListener implements EntryExpiredListener<String, Topology> {
    private IMap<String, Topology> operationsHistoryMap;
    private IMap<String, Topic> topicsMap;
    private ITopic<Topology> operationsTopic;

    public OperationExpirationListener(IMap<String, Topology> operationsHistoryMap, IMap<String, Topic> topicsMap, ITopic<Topology> operationsTopic) {
        this.operationsHistoryMap = operationsHistoryMap;
        this.topicsMap = topicsMap;
        this.operationsTopic = operationsTopic;
    }

    @Override
    public void entryExpired(EntryEvent<String, Topology> event) {
        event.getKey();
        Topology topology = event.getOldValue();
        try {
            topology.getProducedEvents().forEach(this::setLeafs);
        } catch (Exception ex) {
            log.warn("Error while trying to check Leafs:" + ex.getMessage());
        }

        operationsTopic.publish(topology);

        if (!topology.isFinished()) {
            log.warn("Topology Doesn't Finished:" + topology.toString());
            operationsHistoryMap.putIfAbsent(event.getKey(), event.getOldValue(), 1, TimeUnit.DAYS);
        } else {
            log.info("Topology OK:" + topology.toString());
            operationsHistoryMap.putIfAbsent(event.getKey(), event.getOldValue(), 1, TimeUnit.HOURS);
        }

    }

    private void setLeafs(ProducedEvent producedEvent) {
        producedEvent.getListeningServices().forEach((s, iHandledEvent) -> {
            if (iHandledEvent instanceof HandledEvent)
                ((HandledEvent) iHandledEvent).getProducedEvents().forEach(this::setLeafs);
            else if (iHandledEvent instanceof NoneHandled) {
                ServiceData serviceData = topicsMap.get(producedEvent.getTopic()).getServiceDataHashMap().get(s);
                Partition eventPartition = producedEvent.getPartition();
                ((NoneHandled) iHandledEvent).setFinishedAsLeaf(
                        serviceData.getPartitions().get(eventPartition.getNumber()) != null
                                && serviceData.getPartitions().get(eventPartition.getNumber()).getNumber() >= eventPartition.getOffset()
                );
            }
        });
    }
}
