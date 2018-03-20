package com.kloia.eventapis.api.emon.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.Member;
import com.hazelcast.scheduledexecutor.DuplicateTaskException;
import com.hazelcast.scheduledexecutor.IScheduledExecutorService;
import com.hazelcast.scheduledexecutor.IScheduledFuture;
import com.hazelcast.scheduledexecutor.NamedTask;
import com.hazelcast.scheduledexecutor.ScheduledTaskHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TopicServiceScheduler implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private Pattern eventTopicRegex;

    @Autowired
    private ListTopicSchedule listTopicSchedule;

    @Autowired
    private TopicEndOffsetSchedule topicEndOffsetSchedule;


    @Autowired
    private ConsumerOffsetSchedule consumerOffsetSchedule;


    @Autowired
    @Qualifier("hazelcastInstance")
    private HazelcastInstance hazelcastInstance;

    private IScheduledExecutorService scheduledExecutorService;

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        hazelcastInstance.getCluster().getLocalMember().setBooleanAttribute(this.getClass().getSimpleName(), true);

        ILock lock = hazelcastInstance.getLock("emon-TopicServiceScheduler-lock");
        if (!lock.tryLock())
            return;
        try {
            scheduledExecutorService = hazelcastInstance.getScheduledExecutorService(this.getClass().getSimpleName());
            List<Member> schedulerMembers = hazelcastInstance.getCluster().getMembers().stream()
                    .filter(member -> Boolean.TRUE.equals(member.getBooleanAttribute("TopicServiceScheduler"))).collect(Collectors.toList());
            try {
                cancelScheduledTasks(listTopicSchedule);
                scheduledExecutorService.scheduleOnMembersAtFixedRate(listTopicSchedule, schedulerMembers, 0, 10000, TimeUnit.MILLISECONDS);

                cancelScheduledTasks(topicEndOffsetSchedule);
                scheduledExecutorService.scheduleOnMembersAtFixedRate(topicEndOffsetSchedule, schedulerMembers, 0, 500, TimeUnit.MILLISECONDS);


                cancelScheduledTasks(consumerOffsetSchedule);
                scheduledExecutorService.scheduleOnMembersAtFixedRate(consumerOffsetSchedule, schedulerMembers, 0, 500, TimeUnit.MILLISECONDS);


//                log.info("Scheduled :" + future.getHandler().toUrn());
            } catch (DuplicateTaskException e) {
                log.info("Task is Already scheduled with some other instance");
            } catch (Exception e) {
                log.info("Exception while scheduling:" + e.getMessage(), e);
            }
        } finally {
            lock.unlock();
        }

    }

    private void cancelScheduledTasks(NamedTask namedTask) {
        Map<Member, List<IScheduledFuture<Object>>> allScheduledFutures = scheduledExecutorService.getAllScheduledFutures();
        allScheduledFutures.values().forEach(
                iScheduledFutures -> iScheduledFutures.forEach(objectIScheduledFuture -> {
                            ScheduledTaskHandler handler = objectIScheduledFuture.getHandler();
                            if (handler.getTaskName().equals(namedTask.getName())) {
                                log.info("Cancelling Task:" + objectIScheduledFuture.getHandler().getTaskName());
                                IScheduledFuture<Object> scheduledFuture = scheduledExecutorService.getScheduledFuture(objectIScheduledFuture.getHandler());
                                boolean cancel = scheduledFuture.cancel(false);
                                log.info("Cancelled Task:" + cancel);
                                scheduledFuture.dispose();
                            }
                        }
                )
        );
    }

    @PreDestroy
    public void destroy() {
        scheduledExecutorService.shutdown();
    }

}
