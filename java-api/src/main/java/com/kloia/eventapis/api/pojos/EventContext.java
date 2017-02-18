package com.kloia.eventapis.api.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

/**
 * Created by zeldalozdemir on 30/01/2017.
 */
@Data
@AllArgsConstructor
public class EventContext {
    private UUID transactionId;

    private static ThreadLocal<EventContext> eventThreadLocal = new ThreadLocal<>();
    public static EventContext getEventContext(){
        return eventThreadLocal.get();
    }

    public static Event createNewEvent(IEventType iEventType, String[] params){
        EventContext eventContext = eventThreadLocal.get();
        if(eventContext == null){
           eventContext = new EventContext(UUID.randomUUID()); // todo: Notify Event Store
           eventContext.setTransactionId(UUID.randomUUID());
           eventThreadLocal.set(eventContext);
        }
        return new Event(eventContext.getTransactionId(),UUID.randomUUID(),iEventType,EventState.CREATED, params);
    }
    public static void setEventContext(EventContext eventContext){
        eventThreadLocal.set(eventContext);
    }
}
