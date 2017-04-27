package com.kloia.eventapis.api.impl;

import org.springframework.stereotype.Repository;

import java.util.AbstractMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 20/04/2017.
 */
@Repository
public class OperationContext {
    ThreadLocal<Map.Entry<UUID, UUID>> operationContext = new ThreadLocal<Map.Entry<UUID, UUID>>() {
        @Override
        protected Map.Entry<UUID, UUID> initialValue() {
            return super.initialValue();
        }
    };

    public void switchContext(UUID opid) {
        operationContext.set(new AbstractMap.SimpleEntry<UUID, UUID>(opid, null));
    }

    public UUID getContext() {
        Map.Entry<UUID, UUID> entry = operationContext.get();
        return entry == null ? null : entry.getKey();
    }

    public UUID getCommandContext() {
        Map.Entry<UUID, UUID> entry = operationContext.get();
        return entry == null ? null : entry.getValue();
    }

    public void setCommandContext( UUID eventId) throws EventContextException {
        Map.Entry<UUID, UUID> entry = operationContext.get();
        if (entry == null)
            throw new EventContextException("There is no Operation Context");
        else
            entry.setValue(eventId);
    }

    public void clearContext() {
        operationContext.remove();
    }
    public UUID clearCommandContext() {
        Map.Entry<UUID, UUID> entry = operationContext.get();
        if(entry != null)
            return entry.setValue(null);
        return null;
    }
}
