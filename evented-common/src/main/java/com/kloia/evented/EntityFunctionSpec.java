package com.kloia.evented;

import lombok.Getter;
import lombok.NonNull;

/**
 * Created by zeldalozdemir on 21/02/2017.
 */
@Getter
public abstract class EntityFunctionSpec<Entity, EventData> {
    public EntityFunctionSpec(@NonNull Class<Entity> entityType, @NonNull Class<EventData> queryType, @NonNull EntityFunction<Entity, EventData> entityFunction) {
        this.entityType = entityType;
        this.queryType = queryType;
        this.entityFunction = entityFunction;
    }
    @Getter
    private final Class<Entity> entityType;
    @Getter
    private final Class<EventData> queryType;
    @Getter
    private final EntityFunction<Entity, EventData> entityFunction;

}