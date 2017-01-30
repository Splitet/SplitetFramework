package com.kloia.eventapis.pojos;


import com.kloia.eventapis.impl.EventRepository;

/**
 * Created by zeldalozdemir on 30/01/2017.
 */
public class Aggregate {
    EventRepository eventRepository;
    IAggregate iAggregate ;

    public Aggregate(EventRepository eventRepository, IAggregate iAggregate) {
        this.eventRepository = eventRepository;
        this.iAggregate = iAggregate;
    }

    public void execute( String... params){
        iAggregate.execute(eventRepository,params);
    }

    public Event handleEvent(Event event) {
        if(event.getEventType() == IEventType.EXECUTE){
            execute(event.getParams());
            return event.success();
        }else

        return event.fail();
    }
}
