package com.kloia.eventapis.api.emon.configuration.hazelcast;

import com.hazelcast.quorum.QuorumEvent;
import com.hazelcast.quorum.QuorumListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class QuorumListenerForApplicationEvents implements QuorumListener {
    private final ApplicationEventPublisher publisher;

    public QuorumListenerForApplicationEvents(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void onChange(QuorumEvent quorumEvent) {
        if(quorumEvent.isPresent())
            publisher.publishEvent(new InMemoryRestoredEvent(quorumEvent));
        else
            publisher.publishEvent(new InMemoryFailedEvent(quorumEvent));

    }
}
