package com.kloia.eventapis.view;

import com.google.common.reflect.TypeToken;
import lombok.Getter;
import lombok.NonNull;

import java.lang.reflect.ParameterizedType;

/**
 * Created by zeldalozdemir on 21/02/2017.
 */
@Getter
public abstract class EntityFunctionSpec<EntityT, EventDataT> {
    @Getter
    private final EntityFunction<EntityT, EventDataT> entityFunction;

    public EntityFunctionSpec(@NonNull EntityFunction<EntityT, EventDataT> entityFunction) {
        this.entityFunction = entityFunction;
    }

    public Class<EventDataT> getQueryType() {
        ParameterizedType type = (ParameterizedType) TypeToken.of(this.getClass()).getSupertype(EntityFunctionSpec.class).getType();
        try {
            return (Class<EventDataT>) Class.forName(type.getActualTypeArguments()[1].getTypeName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Class<EntityT> getEntityType() {
        ParameterizedType type = (ParameterizedType) TypeToken.of(this.getClass()).getSupertype(EntityFunctionSpec.class).getType();
        try {
            return (Class<EntityT>) Class.forName(type.getActualTypeArguments()[0].getTypeName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

}