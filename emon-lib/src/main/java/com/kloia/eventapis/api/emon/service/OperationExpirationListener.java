package com.kloia.eventapis.api.emon.service;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.IMap;
import com.hazelcast.map.listener.EntryExpiredListener;
import com.kloia.eventapis.api.emon.domain.Topology;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class OperationExpirationListener implements EntryExpiredListener<String, Topology> {
    private IMap<String, Topology> operationsHistoryMap;

    public OperationExpirationListener(IMap<String, Topology> operationsHistoryMap) {
        this.operationsHistoryMap = operationsHistoryMap;
    }

    @Override
    public void entryExpired(EntryEvent<String, Topology> event) {
        event.getKey();
        Topology topology = event.getOldValue();
        if (!topology.isFinished()) {
            log.error("Topology Doesn't Finished:" + topology.toString());
            operationsHistoryMap.putIfAbsent(event.getKey(), event.getOldValue(), 1, TimeUnit.DAYS);
        } else {
            log.info("Topology OK:" + topology.toString());
            operationsHistoryMap.putIfAbsent(event.getKey(), event.getOldValue(), 1, TimeUnit.HOURS);
        }

    }
}
