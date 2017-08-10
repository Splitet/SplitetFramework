package com.kloia.eventapis.exception;

/**
 * Created by zeldalozdemir on 21/02/2017.
 */
public class EventStoreException extends Exception {
    public EventStoreException() {
    }

    public EventStoreException(String message) {
        super(message);
    }

    public EventStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventStoreException(Throwable cause) {
        super(cause);
    }

    public EventStoreException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
