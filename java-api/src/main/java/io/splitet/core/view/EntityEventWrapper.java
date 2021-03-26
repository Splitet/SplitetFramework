package io.splitet.core.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.splitet.core.cassandra.EntityEvent;
import io.splitet.core.exception.EventStoreException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;

/**
 * Created by zeldalozdemir on 07/02/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntityEventWrapper<E> {

    private Class<E> type;
    private ObjectMapper objectMapper;
    private EntityEvent entityEvent;

    public E getEventData() throws EventStoreException {
        try {
            return objectMapper.readValue(entityEvent.getEventData(), type);
        } catch (IOException e) {
            throw new EventStoreException(e.getMessage(), e);
        }
    }
}
