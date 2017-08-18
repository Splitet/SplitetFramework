package com.kloia.eventapis.cassandra;

import com.kloia.eventapis.exception.EventStoreException;

public class ConcurrentEventException extends Exception {
    private Exception exception;

    public ConcurrentEventException() {
    }

    public ConcurrentEventException(String message) {
        super(message);
    }
}
