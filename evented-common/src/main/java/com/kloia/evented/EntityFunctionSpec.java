package com.kloia.evented;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeToken;
import com.kloia.evented.domain.EntityEvent;
import lombok.Getter;
import lombok.NonNull;
import sun.reflect.generics.tree.ClassTypeSignature;
import sun.reflect.generics.tree.SimpleClassTypeSignature;
import sun.reflect.generics.tree.TypeArgument;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.Date;

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