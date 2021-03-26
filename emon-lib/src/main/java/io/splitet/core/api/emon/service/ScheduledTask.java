package io.splitet.core.api.emon.service;

import com.hazelcast.core.IMap;
import com.hazelcast.scheduledexecutor.NamedTask;
import com.hazelcast.spring.context.SpringAware;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;

@Slf4j
@SpringAware
@Component
abstract class ScheduledTask implements Runnable, NamedTask, Serializable {

    public static final String LAST_SUCCESS_PREFIX = "_LAST_SUCCESS";
    transient IMap<String, Long> metaMap;
    transient Long scheduleRateInMillis;

    @Override
    public void run() {

        StopWatch stopWatch = new StopWatch();
        boolean isSuccess;
        try {
            isSuccess = runInternal(stopWatch);
            if (isSuccess) {
                metaMap.set(getLastSuccessKey(), System.currentTimeMillis());
            }
        } catch (InterruptedException | ExecutionException e) {
            log.warn("Error While trying to run ScheduledTask: " + e.getMessage(), e);
        }
        log.debug(stopWatch.prettyPrint());
    }

    private String getLastSuccessKey() {
        return this.getName() + LAST_SUCCESS_PREFIX;
    }

    abstract boolean runInternal(StopWatch stopWatch) throws InterruptedException, ExecutionException;

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    Long getScheduleRateInMillis() {
        return scheduleRateInMillis;
    }

    abstract void setScheduleRateInMillis(Long scheduleRateInMillis);

    @Autowired
    public void setMetaMap(IMap metaMap) {
        this.metaMap = metaMap;
    }

    public boolean isRunning() {
        Long lastSuccessTime = metaMap.get(getLastSuccessKey());
        return (lastSuccessTime + getScheduleRateInMillis() + 10000L) > System.currentTimeMillis();
    }
}
