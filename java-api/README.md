* Aggregate Style (Transaction Definition)
    * Factory.command(Parameters,EventImplementation,[FailLogic]).
        * events(EventCommand,....).
        * after().
        * events(EventCommand,....).
        * build()
        
       
* Event Register (Microservice startups)
    * Eventstore.register(EventImplementation, [FailLogic])
    
* EventCommand
    * name (Microservice Name ?)
    * Parameters

* EventImplementation 
    * name
    * execute (Parameters)
    
* FailLogic 
    * Automatic
    * Custom
        * Execute (transaction, cause,)