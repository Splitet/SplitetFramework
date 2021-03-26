package com.kloia.eventapis.api.emon.service;

import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.kloia.eventapis.api.emon.domain.Topology;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class OperationsBroadcaster implements MessageListener<Topology> {

    @Autowired
    private ITopic<Topology> operationsTopic;

    private final List<SseEmitter> emitters = Collections.synchronizedList(new ArrayList<>());

    @PostConstruct
    private void postConstruct() {
        operationsTopic.addMessageListener(this);
    }


    public void registerEmitter(SseEmitter sseEmitter) {
        emitters.add(sseEmitter);
    }

    public void deregisterEmitter(SseEmitter sseEmitter) {
        emitters.remove(sseEmitter);
    }

    @Override
    public void onMessage(Message<Topology> message) {
        emitters.removeIf(sseEmitter -> {
            try {
                sseEmitter.send(message.getMessageObject());
                return false;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return true;
            }
        });
    }
}
