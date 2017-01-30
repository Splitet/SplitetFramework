package com.kloia.eventapis.impl;

import com.kloia.eventapis.pojos.Aggregate;
import com.kloia.eventapis.pojos.IAggregate;

/**
 * Created by zeldalozdemir on 30/01/2017.
 */
public class AggregateBuilder {
    private EventRepository eventRepository;

    public AggregateBuilder(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Aggregate createAggregate(IAggregate aggregate) {
        return new Aggregate(eventRepository, aggregate);
    }


}
