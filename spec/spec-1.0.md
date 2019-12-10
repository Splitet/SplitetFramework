service:  Microservices in general, 2 types in general.  BPM: Service
    command-service: Handler for Command/Events. (BPM Process)
    query-service: Query and Snapshot Handler. (BPM Process, Listener)
    
event: Events Sourcing. fired by command-service, can be listened my zero or more command-service
    OP_EVENT: Ordinary Event
    TX_FAIL: Failure events
    TX_SUCCESS: Successful events
    OP_SINGLE: OP_EVENT+TX_SUCCESS together
    
Snapshot: 
    status: NOT_STARTED/IN_PROGRESS/DONE/ERROR
    
```
Operation
    id: operation-id  (transaction-id -> uuid)
    handler-name: (command-service) (BPM Process)
    service-name:
    events:
            Event1:
                listeners:
                    EventListener1: (command-service) (BPM Process)
                    EventListener2: (command-service) (BPM Process)
                ...
            Event2: 
                listeners:
                    EventListener3: (command-service) (BPM Process)
                        Event3:
                              ...
                        Event4:
                              ...
            ...
        
```          