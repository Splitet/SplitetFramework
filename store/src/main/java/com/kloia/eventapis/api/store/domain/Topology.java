package com.kloia.eventapis.api.store.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class Topology implements Serializable {

    private EventHandler head;

    private final String initiatorService;
    private final String initiatorCommand;


    public Topology(EventHandler head, String initiatorCommand) {
        this.head = head;
        this.initiatorService = head.getSender();
        this.initiatorCommand = initiatorCommand;
    }

    public String getInitiatorService(){
        return head.getSender();
    }

    public boolean putNextEventHandler(EventHandler eventHandler) {
        return head.attachHandler(eventHandler);
    }
}
