package com.kloia.eventapis.api.store.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.common.EventType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@Slf4j
@JsonTypeName("event")
public class EventHandler implements IEventHandler {

    private final String topic;
    private final String sender;
    private final EventType eventType;
    private final EventKey eventKey;
    private int numberOfVisit = 1;
    private Map<String, IEventHandler> publishedEvents;

    public EventHandler(String topic, String sender, EventType eventType, EventKey eventKey, List<String> targetList) {
        this.topic = topic;
        this.sender = sender;
        this.eventType = eventType;
        this.eventKey = eventKey;
        if (targetList != null)
            publishedEvents = targetList.stream().collect(Collectors.toMap(Function.identity(), s -> NoneHandler.noneHandler));
        else
            publishedEvents = new HashMap<>();
    }

    @Override
    public boolean attachHandler(EventHandler eventHandler) {
        IEventHandler result = publishedEvents.computeIfPresent(eventHandler.getSender(), (s, iEventHandler) -> {
            if (iEventHandler.equals(NoneHandler.noneHandler)) {
                log.info("Attaching Event into: "+this+" New Event:"+eventHandler);
                return eventHandler;
            } else {
                EventHandler oldEventHandler = (EventHandler) iEventHandler;
                if(oldEventHandler.getTopic().equals(eventHandler.getTopic())){
                    log.info("Duplicate Event Handle for:" + eventHandler);
                    eventHandler.incrementNumberOfVisit();
                    log.info("Attaching Event into: "+this+" New Event:"+eventHandler);
                    return eventHandler;
                }else {
                    return iEventHandler;
                }
            }
        });
        return result == eventHandler || publishedEvents.values().stream().anyMatch(iEventHandler -> iEventHandler.attachHandler(eventHandler));
    }

    @Override
    public void incrementNumberOfVisit() {
        numberOfVisit++;
    }
}
