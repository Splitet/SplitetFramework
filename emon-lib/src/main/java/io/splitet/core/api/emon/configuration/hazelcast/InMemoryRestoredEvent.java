package io.splitet.core.api.emon.configuration.hazelcast;

import org.springframework.context.ApplicationEvent;

public class InMemoryRestoredEvent extends ApplicationEvent {
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public InMemoryRestoredEvent(Object source) {
        super(source);
    }
}
