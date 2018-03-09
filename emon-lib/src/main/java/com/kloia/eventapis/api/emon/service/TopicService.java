package com.kloia.eventapis.api.emon.service;

import com.kloia.eventapis.api.emon.configuration.StoreConfiguration;
import kafka.admin.ConsumerGroupCommand;
import kafka.utils.ZkUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import scala.Option;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.Seq;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
@Slf4j
public class TopicService implements Runnable {
    private StoreConfiguration storeConfiguration;
    @Getter
    private Map<String, List<String>> topicServiceList;
    @Getter
    private Map<String, List<String>> serviceList;

    private String groupId;
    private ConsumerGroupCommand.KafkaConsumerGroupService consumerGroupService;
    private ZkUtils zkUtils;
    private Pattern eventTopicRegex;


    public TopicService(StoreConfiguration storeConfiguration) {
        this.storeConfiguration = storeConfiguration;
    }

    private static Long getLong(Option<Object> s) {
        if (s.isEmpty())
            return 0L;
        else

            return Long.valueOf(s.get().toString());
    }

    public String[] queryTopicListAsArr() throws InterruptedException {
//        Map<String, Object> producerProperties = storeConfiguration.getEventBus().buildProducerProperties();
//        KafkaAdminClient adminClient = (KafkaAdminClient) AdminClient.create(producerProperties);
//
//        return adminClient.listTopics().namesToListings().get().keySet().toArray(new String[0]);
        return topicServiceList.keySet().toArray(new String[0]);
    }

    @PostConstruct
    public void init() {
        String bootstrapServers = String.join(",", storeConfiguration.getEventBus().getBootstrapServers());
        String zookeeperServers = String.join(",", storeConfiguration.getEventBus().getZookeeperServers());
        eventTopicRegex = Pattern.compile(storeConfiguration.getEventTopicRegex());


        consumerGroupService = new ConsumerGroupCommand.KafkaConsumerGroupService(
                new ConsumerGroupCommand.ConsumerGroupCommandOptions(new String[]{"--zookeeper", zookeeperServers, "--bootstrap-server", bootstrapServers, "--group", "empty"}));
        zkUtils = ZkUtils.apply(zookeeperServers, 3000, 3000, false);

        groupId = storeConfiguration.getEventBus().getConsumer().getGroupId();

        run(); // first run
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(this, 10, 3600, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        List<String> groupList = JavaConversions.seqAsJavaList(consumerGroupService.listGroups());
        Map<String, List<String>> serviceList = new HashMap<>();
        Map<String, List<String>> topicServiceList = new HashMap<>();
        JavaConversions.seqAsJavaList(zkUtils.getAllTopics()).stream().filter(s -> eventTopicRegex.matcher(s).matches()).forEach(s -> topicServiceList.put(s, new ArrayList<>()));

        groupList.forEach(consumer -> {
            if (!consumer.equals(groupId) && !consumer.endsWith("capability")) // filter out group id
                try {
                    List<String> serviceTopics = serviceList.compute(consumer, (s, strings) -> strings != null ? strings : new ArrayList<>());
                    Tuple2<Option<String>, Option<Seq<ConsumerGroupCommand.PartitionAssignmentState>>> describeGroup = consumerGroupService.collectGroupAssignment(consumer);
                    JavaConversions.seqAsJavaList(describeGroup._2.get()).forEach(partitionAssignmentState -> {
                                String topic = partitionAssignmentState.topic().getOrElse(() -> null);
                                if (topic != null && eventTopicRegex.matcher(topic).matches()) {
                                    serviceTopics.add(topic);
                                    topicServiceList.compute(topic, (service, services) -> {
                                        if (services == null)
                                            services = new ArrayList<>();
                                        services.add(consumer);
                                        return services;
                                    });
                                    log.debug("\t Consumes: " + partitionAssignmentState.topic() + " - " + partitionAssignmentState.offset() + " all: " + partitionAssignmentState.toString());
                                    if (Long.compare(getLong(partitionAssignmentState.offset()), getLong(partitionAssignmentState.logEndOffset())) != 0)
                                        log.info("\t Missing: " + partitionAssignmentState.topic() + " - " + partitionAssignmentState.offset() + "/" + partitionAssignmentState.logEndOffset());
                                }
                            }
                    );
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
        });
        this.serviceList = serviceList;
        this.topicServiceList = topicServiceList;
    }
}
