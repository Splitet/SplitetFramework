package com.kloia.eventapis.cassandra;

public class ConcurrentEventException extends Exception {

    public ConcurrentEventException() {
    }

    public ConcurrentEventException(String message) {
        super(message);
    }
}
