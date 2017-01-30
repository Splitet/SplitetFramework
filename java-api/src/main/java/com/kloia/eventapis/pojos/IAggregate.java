package com.kloia.eventapis.pojos;


import com.kloia.eventapis.impl.EventRepository;

/**
 * Created by zeldalozdemir on 30/01/2017.
 */
public interface IAggregate {
    void execute(EventRepository eventRepository, String... params);
}
/**/