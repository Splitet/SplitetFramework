package com.kloia.eventapis.cassandra;

public class ConcurrentEventException extends Exception {
    private Exception exception;

    public ConcurrentEventException() {
    }

    public ConcurrentEventException(String message) {
        super(message);
    }
}
