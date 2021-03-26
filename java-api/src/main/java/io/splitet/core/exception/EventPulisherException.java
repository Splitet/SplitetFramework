package io.splitet.core.exception;

/**
 * Created by zeldalozdemir on 21/02/2017.
 */
public class EventPulisherException extends Exception {
    public EventPulisherException() {
    }

    public EventPulisherException(String message) {
        super(message);
    }

    public EventPulisherException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventPulisherException(Throwable cause) {
        super(cause);
    }

    public EventPulisherException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
