package com.kloia.eventapis.pojos;

import lombok.Data;

/**
 * Created by zeldalozdemir on 30/01/2017.
 */
public enum IEventType {
    EXECUTE,
    ROLLBACK;
/*
    public static final IEventType EXECUTE = new IEventType() {   };
    public static final IEventType ROLLBACK = new IEventType() {   };*/
}
