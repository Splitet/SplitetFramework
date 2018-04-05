package com.kloia.eventapis.api.emon.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.common.EventType;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProducedEvent implements IProducedEvent {

    private final String topic;
    private final String sender;
    private final EventType eventType;
    private final EventKey eventKey;
    private String aggregateId;
    private int numberOfVisit = 1;
    private long opDate;
    private OperationEvent operation;
    private Map<String, IHandledEvent> listeningServices;

    public ProducedEvent(String topic, String sender, String aggregateId, EventType eventType, EventKey eventKey, List<String> targetList, long opDate) {
        this.topic = topic;
        this.sender = sender;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.eventKey = eventKey;
        this.opDate = opDate;
        if (targetList != null)
            listeningServices = targetList.stream().collect(Collectors.toMap(Function.identity(), s -> new NoneHandled()));
        else
            listeningServices = new HashMap<>();
    }

    @Override
    public boolean attachHandler(ProducedEvent producedEvent) {
        if (Objects.equals(topic, producedEvent.getAggregateId())) {
            IHandledEvent existingEvent = listeningServices.get(producedEvent.getSender());
            if (existingEvent instanceof NoneHandled) {
                log.debug("Attaching Event into: " + this + " New Event:" + producedEvent);
                producedEvent.setOperation(((NoneHandled) existingEvent).getOperation()); // if any
                listeningServices.put(producedEvent.getSender(), new HandledEvent(producedEvent, producedEvent.getSender(), topic));
            } else {
                HandledEvent oldEventHandler = (HandledEvent) existingEvent;
                oldEventHandler.attachProducedEvent(producedEvent);
            }
            return true;
        } else
            return listeningServices.values().stream()
                    .filter(handledEvent -> handledEvent instanceof HandledEvent)
                    .flatMap(handledEvent -> ((HandledEvent) handledEvent).getProducedEvents().stream())
                    .anyMatch(event -> event.attachHandler(producedEvent));
    }

    @Override
    public void incrementNumberOfVisit() {
        numberOfVisit++;
    }

    @Override
    @JsonIgnore
    public boolean isFinished() {
        return listeningServices.isEmpty()
                || listeningServices.values().stream().allMatch(IHandledEvent::isFinished);
    }

    @Override
    public boolean attachOperation(OperationEvent operation) {
        if (Objects.equals(operation.getSender(), getSender())
                && Objects.equals(operation.getAggregateId(), topic)
                && getEventType() == EventType.OP_FAIL
                && operation.getTransactionState() == TransactionState.TXN_FAILED) {
            this.operation = operation;
            return true;
        }

        if (Objects.equals(operation.getSender(), getSender())
                && Objects.equals(operation.getAggregateId(), topic)
                && (getEventType() == EventType.OP_SUCCESS || getEventType() == EventType.OP_SINGLE)
                && operation.getTransactionState() == TransactionState.TXN_SUCCEEDED) {
            this.operation = operation;
            return true;
        }

        if (Objects.equals(operation.getAggregateId(), topic)) {
            if (listeningServices.entrySet().stream().anyMatch(
                    keyValue -> {
                        if (Objects.equals(operation.getSender(), keyValue.getKey())) {
                            keyValue.getValue().setOperation(operation);
                            return true;
                        } else return false;

                    })) {
                return true;
            }
        }
        return listeningServices.values().stream().anyMatch(iEventHandler -> iEventHandler.attachOperation(operation));
    }

    @Override
    public void setOperation(OperationEvent operation) {
        this.operation = operation;
    }
}
