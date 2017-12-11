package com.kloia.eventapis.api.store.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.common.EventType;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.pojos.TransactionState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private Operation operation;
    private Map<String, IEventHandler> publishedEvents;

    public EventHandler(String topic, String sender, EventType eventType, EventKey eventKey, List<String> targetList) {
        this.topic = topic;
        this.sender = sender;
        this.eventType = eventType;
        this.eventKey = eventKey;
        if (targetList != null)
            publishedEvents = targetList.stream().collect(Collectors.toMap(Function.identity(), s -> new NoneHandler()));
        else
            publishedEvents = new HashMap<>();
    }

    @Override
    public boolean attachHandler(EventHandler eventHandler) {
        IEventHandler result = publishedEvents.computeIfPresent(eventHandler.getSender(), (s, iEventHandler) -> {
            if (iEventHandler instanceof NoneHandler) {
                log.info("Attaching Event into: " + this + " New Event:" + eventHandler);
                eventHandler.setOperation(((NoneHandler) iEventHandler).getOperation()); // if any
                return eventHandler;
            } else {
                EventHandler oldEventHandler = (EventHandler) iEventHandler;
                if (oldEventHandler.getTopic().equals(eventHandler.getTopic())) {
                    log.info("Duplicate Event Handle for:" + eventHandler);
                    eventHandler.incrementNumberOfVisit();
                    log.info("Attaching Event into: " + this + " New Event:" + eventHandler);
                    return eventHandler;
                } else {
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

    @Override
    @JsonIgnore
    public boolean isFinished() {
        return operation != null
                && operation.getTransactionState() == TransactionState.TXN_FAILED
                || publishedEvents.isEmpty()
                || publishedEvents.values().stream().allMatch(IEventHandler::isFinished);
    }

    @Override
    public boolean attachOperation(Operation operation) {
        if (Objects.equals(operation.getSender(), getSender())
                && Objects.equals(operation.getAggregateId(), topic)
                && operation.getTransactionState() == TransactionState.TXN_SUCCEDEED) {
            this.operation = operation;
            return true;
        }

        if (Objects.equals(operation.getSender(), getSender())
                && Objects.equals(operation.getAggregateId(), topic)
                && getEventType() == EventType.OP_FAIL
                && operation.getTransactionState() == TransactionState.TXN_FAILED) {
            this.operation = operation;
            return true;
        }
        if (Objects.equals(operation.getAggregateId(), topic)) {
            if (publishedEvents.entrySet().stream().anyMatch(
                    keyValue -> {
                        if (Objects.equals(operation.getSender(), keyValue.getKey())) {
                            keyValue.getValue().setOperation(operation);
                            return true;
                        } else return false;

                    })) {
                return true;
            }
        }

        return publishedEvents.values().stream().anyMatch(iEventHandler -> iEventHandler.attachOperation(operation));

/*        if (Objects.equals(operation.getSender(), getSender()) && Objects.equals(operation.getAggregateId(), topic)) {
            if (operation.getTransactionState() == TransactionState.TXN_FAILED && eventType != EventType.EVENT) {
                log.error("Operation Failed in Non-EVENT type:" + eventType);
            }
            if (operation.getTransactionState() == TransactionState.TXN_SUCCEDEED && (eventType != EventType.OP_SUCCESS && eventType != EventType.OP_SINGLE)) {
                log.error("Operation Success in Non-SINGLE/SUCCESS type:" + eventType);
            }
            transactionState = operation.getTransactionState();
            return true;
        } else
            return publishedEvents.values().stream().anyMatch(iEventHandler -> iEventHandler.attachOperation(operation));*/

    }

    @Override
    public void setOperation(Operation operation) {
        this.operation = operation;
    }
}
