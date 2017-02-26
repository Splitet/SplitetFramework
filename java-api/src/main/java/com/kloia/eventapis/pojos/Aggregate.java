package com.kloia.eventapis.pojos;


import com.kloia.eventapis.api.impl.OperationRepository;

/**
 * Created by zeldalozdemir on 30/01/2017.
 */
public class Aggregate {
    OperationRepository operationRepository;
    IAggregate iAggregate ;

    public Aggregate(OperationRepository operationRepository, IAggregate iAggregate) {
        this.operationRepository = operationRepository;
        this.iAggregate = iAggregate;
    }

    public void execute( String... params){
        iAggregate.execute(operationRepository,params);
    }

    public Event handleEvent(Event event) {
        if(event.getEventType() == IEventType.EXECUTE){
            execute(event.getParams());
            return event.success();
        }else

        return event.fail();
    }
}
