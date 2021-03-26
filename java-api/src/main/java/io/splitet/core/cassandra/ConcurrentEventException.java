package io.splitet.core.cassandra;

public class ConcurrentEventException extends Exception {

    public ConcurrentEventException() {
    }

    public ConcurrentEventException(String message) {
        super(message);
    }
}
