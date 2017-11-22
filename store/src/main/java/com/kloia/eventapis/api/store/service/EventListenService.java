package com.kloia.eventapis.api.store.service;

import com.kloia.eventapis.api.store.configuration.StoreConfiguration;
import com.kloia.eventapis.kafka.JsonDeserializer;
import com.kloia.eventapis.kafka.PublishedEventWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class EventListenService {

    @Autowired
    private StoreConfiguration storeConfiguration ;
    @Autowired
    private TopicService topicService;
    @Autowired
    private TopologyService topologyService;

    private ConcurrentMessageListenerContainer<String, PublishedEventWrapper> messageListenerContainer;


    @PostConstruct
    public void startListen() throws ExecutionException, InterruptedException {
        Map<String, Object> consumerProperties = storeConfiguration.getEventBus().buildConsumerProperties();

        DefaultKafkaConsumerFactory<String, PublishedEventWrapper> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProperties, new StringDeserializer(), new JsonDeserializer<>(PublishedEventWrapper.class));

        ContainerProperties containerProperties = new ContainerProperties(topicService.queryTopicListAsArr());
        containerProperties.setMessageListener(topologyService);
        messageListenerContainer = new ConcurrentMessageListenerContainer<>(consumerFactory,
                containerProperties);
        messageListenerContainer.setBeanName("OpCenter");
        messageListenerContainer.start();
    }

    @PreDestroy
    public void stopListen(){
        if(messageListenerContainer != null && messageListenerContainer.isRunning())
            messageListenerContainer.stop();
    }
}
