package com.kloia.eventapis.api.emon.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.scheduledexecutor.DuplicateTaskException;
import com.hazelcast.scheduledexecutor.IScheduledExecutorService;
import com.hazelcast.scheduledexecutor.IScheduledFuture;
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

@Service
@Slf4j
public class TopicServiceScheduler implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private Pattern eventTopicRegex;

    @Autowired
    private ListTopicSchedule listTopicSchedule;


    @Autowired
    @Qualifier("hazelcastInstance")
    private HazelcastInstance hazelcastInstance;

    private IScheduledExecutorService scheduledExecutorService;

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {


//        ILock lock = hazelcastInstance.getLock("emon-TopicServiceScheduler-lock");
//        if (!lock.tryLock())
//            return;
        try {
            scheduledExecutorService = hazelcastInstance.getScheduledExecutorService(this.getClass().getSimpleName());
            try {
                Map<Member, List<IScheduledFuture<Object>>> allScheduledFutures = scheduledExecutorService.getAllScheduledFutures();
                allScheduledFutures.values().forEach(
                        iScheduledFutures -> iScheduledFutures.forEach(objectIScheduledFuture -> {
                                    ScheduledTaskHandler handler = objectIScheduledFuture.getHandler();
                                    if (handler.getTaskName().equals(listTopicSchedule.getName())) {
                                        log.info("Cancelling Task:" + objectIScheduledFuture.getHandler().getTaskName());
                                        IScheduledFuture<Object> scheduledFuture = scheduledExecutorService.getScheduledFuture(objectIScheduledFuture.getHandler());
                                        boolean cancel = scheduledFuture.cancel(false);
                                        log.info("Cancelled Task:" + cancel);
                                        scheduledFuture.dispose();
                                    }
                                }
                        )
                );

                IScheduledFuture<?> future = scheduledExecutorService.scheduleAtFixedRate(listTopicSchedule, 0, 10000, TimeUnit.MILLISECONDS);
                log.info("Scheduled :" + future.getHandler().toUrn());
            } catch (DuplicateTaskException e) {
                log.info("Task is Already scheduled with some other instance");
            } catch (Exception e) {
                log.info("Exception while scheduling:" + e.getMessage(), e);
            }
        } finally {
//            lock.unlock();
        }

    }

/*    @Override
    public void run() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("adminClient.listAllConsumerGroupsFlattened()");
        List<String> groupList = JavaConversions.seqAsJavaList(adminClient.listAllConsumerGroupsFlattened())
                .stream().map(GroupOverview::groupId).collect(Collectors.toList());

        log.debug("consumerGroups: " + groupList.toString());
        stopWatch.stop();

        stopWatch.start("client.listTopics()");
        try {
            Collection<TopicListing> topicListings = client.listTopics().listings().get();
            topicListings.forEach(s -> topics.collect(s.name()));
            DescribeTopicsResult describeTopicsResult = client.describeTopics(topics.keySet());
            describeTopicsResult.all().get().forEach(
                    (topic, topicDescription) -> topics.get(topic).setPartitions(
                            topicDescription.partitions().stream().map(TopicPartitionInfo::partition).collect(Collectors.toList()))
            );
        } catch (InterruptedException | ExecutionException e) {
            log.warn("Error While trying to fetch Topic List " + e.getMessage(), e);
        }
        stopWatch.stop();


        stopWatch.start("collectEndOffsets");
        try {
            List<TopicPartition> collect = topics.entrySet().stream().flatMap(
                    topic -> topic.getValue().getPartitions().stream().map(partition -> new TopicPartition(topic.getKey(), partition))
            ).collect(Collectors.toList());
            java.util.Map<TopicPartition, Long> map = kafkaConsumer.endOffsets(collect);
            map.forEach((topicPartition, endOffset) -> topics.collect(topicPartition.topic(), endOffset));
            log.debug("collectEndOffsets:" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopWatch.stop();

        stopWatch.start("collectGroupOffsets");
        groupList.forEach(consumer -> {
            if (shouldCollectConsumer(consumer)) {
                try {
                    Map<TopicPartition, Object> listGroupOffsets = adminClient.listGroupOffsets(consumer);
                    java.util.Map<TopicPartition, Object> map = JavaConversions.mapAsJavaMap(listGroupOffsets);
                    java.util.Map<String, Long> result = map.entrySet().stream().collect(
                            Collectors.toMap(
                                    entry -> entry.getKey().topic(), entry -> (Long) entry.getValue(), Math::max));
                    for (java.util.Map.Entry<String, Long> entry : result.entrySet()) {
                        if (shouldCollectEvent(entry.getKey())) {
                            topics.collect(entry.getKey(), consumer, entry.getValue());
                        }
                    }
                    log.debug(listGroupOffsets.toString());
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
        stopWatch.stop();

        log.debug("Operation Events:" + topics.get("operation-events"));
        log.debug(stopWatch.prettyPrint());
    }*/

    @PreDestroy
    public void destroy() {
        scheduledExecutorService.shutdown();
    }

    private boolean shouldCollectEvent(String topic) {
        return topic != null && (eventTopicRegex.matcher(topic).matches() || topic.equals("operation-events"));
    }

    private boolean shouldCollectConsumer(String consumer) {
        //todo from
        return consumer.endsWith("command-query") || consumer.endsWith("command");
    }

}
