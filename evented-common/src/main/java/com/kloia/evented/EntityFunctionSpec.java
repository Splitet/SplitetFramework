package com.kloia.evented;

import lombok.Getter;
import lombok.NonNull;
import org.springframework.core.ResolvableType;

/**
 * Created by zeldalozdemir on 21/02/2017.
 */
@Getter
public abstract class EntityFunctionSpec<Entity, EventData> {
    public EntityFunctionSpec(@NonNull EntityFunction<Entity, EventData> entityFunction) {
        this.entityFunction = entityFunction;
    }
    @Getter
    private final EntityFunction<Entity, EventData> entityFunction;

    public  Class<EventData> getQueryType() {
        return (Class<EventData>) ResolvableType.forInstance(this).getSuperType().resolveGenerics()[1];
    }

    public  Class<Entity> getEntityType() {
        return (Class<Entity>) ResolvableType.forInstance(this).getSuperType().resolveGenerics()[0];
    }
}