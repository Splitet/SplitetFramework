package com.kloia.eventapis.api.pojos;

/**
 * Created by zeldalozdemir on 30/01/2017.
 */
public interface IEventType {
    public static final IEventType EXECUTE = new IEventType() {   };
    public static final IEventType ROLLBACK = new IEventType() {   };
}
