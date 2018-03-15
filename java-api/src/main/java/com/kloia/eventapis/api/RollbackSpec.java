package com.kloia.eventapis.api;

import com.google.common.reflect.TypeToken;
import com.kloia.eventapis.common.RecordedEvent;

import java.lang.reflect.ParameterizedType;
import java.util.AbstractMap;
import java.util.Map;

/**
 * Created by zeldalozdemir on 21/02/2017.
 */
public interface RollbackSpec<P extends RecordedEvent> {
    void rollback(P event);

    default Map.Entry<String, Class<RecordedEvent>> getNameAndClass() {
        ParameterizedType type = (ParameterizedType) TypeToken.of(this.getClass()).getSupertype(RollbackSpec.class).getType();
        try {
            Class<RecordedEvent> publishedEventClass = (Class<RecordedEvent>) Class.forName(type.getActualTypeArguments()[0].getTypeName());
            return new AbstractMap.SimpleEntry<>(publishedEventClass.getSimpleName(), publishedEventClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}