package com.kloia.eventapis.view;

import com.google.common.reflect.TypeToken;
import lombok.Getter;
import lombok.NonNull;

import java.lang.reflect.ParameterizedType;

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

    public  Class<EventData> getQueryType()  {
        ParameterizedType type = (ParameterizedType) TypeToken.of(this.getClass()).getSupertype(EntityFunctionSpec.class).getType();
        try {
            return (Class<EventData>) Class.forName(type.getActualTypeArguments()[1].getTypeName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public  Class<Entity> getEntityType() {
        ParameterizedType type = (ParameterizedType) TypeToken.of(this.getClass()).getSupertype(EntityFunctionSpec.class).getType();
        try {
            return (Class<Entity>) Class.forName(type.getActualTypeArguments()[0].getTypeName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

}