# Eventapis Store
An event database to enable event sourcing and distributed transactions for applications that use the microservice architecture.

# Queues
* Queue1 (id-QueueName: AGGREGATE_PARENT, Example: ORDER,   )
    * Events: 
        * id: UUID
        * EventName: ORDER_CREATED (Ex: Order Created) 
        * EventType: EXECUTE|FAIL
        * Params (Event Specific Data)
        * Transaction Ref (?)
        
        

# Aggregate
* Name: AGGREGATE_NAME (ex: SEND_PAYMENT)
* Related Events
    * Execute Events (Ex: ORDER_CREATED|EXECUTE)
    * Fail Events (Ex: ORDER_CREATED|FAIL)


# Transaction
* id: UUID
* StarterAggregate: Aggregate
* Events
    * Event1
    * Event2 ...
* State  