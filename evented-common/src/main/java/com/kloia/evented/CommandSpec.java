package com.kloia.evented;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
/*
@Data
@AllArgsConstructor
public abstract class CommandSpec<T extends Entity, D extends IEventDto> {
    private String commandName;
    protected ObjectMapper objectMapper;
    protected IEventRepository<T> eventRepository;
//    private AggregateFunction<T> apply;

    public abstract void processCommand(D eventDto) throws EventStoreException;


    protected EntityEvent createEvent(EventKey eventKey, String status, Object eventData, UUID key) throws JsonProcessingException {
        String description = objectMapper.writer().writeValueAsString(eventData);
        return new EntityEvent(eventKey, key, new Date(), commandName, status, description);
    }


}
*/
