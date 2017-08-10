package com.kloia.eventapis.api.impl;


import java.util.AbstractMap;
import java.util.Map;
import java.util.UUID;


public class OperationContext {
    ThreadLocal<Map.Entry<String, String>> operationContext = new ThreadLocal<Map.Entry<String, String>>() {
        @Override
        protected Map.Entry<String, String> initialValue() {
            return super.initialValue();
        }
    };

    public void switchContext(String opId) {
        operationContext.set(new AbstractMap.SimpleEntry<String, String>(opId, null));
    }

    public String getContext() {
        Map.Entry<String, String> entry = operationContext.get();
        return entry == null ? null : entry.getKey();
    }

    public String getCommandContext() {
        Map.Entry<String, String> entry = operationContext.get();
        return entry == null ? null : entry.getValue();
    }

    public void setCommandContext(String eventId) throws EventContextException {
        Map.Entry<String, String> entry = operationContext.get();
        if (entry == null)
            throw new EventContextException("There is no Operation Context");
        else
            entry.setValue(eventId);
    }

    public void clearContext() {
        operationContext.remove();
    }

    public String clearCommandContext() {
        Map.Entry<String, String> entry = operationContext.get();
        if(entry != null)
            return entry.setValue(null);
        return null;
    }

    public String generateContext() {
        UUID uuid = UUID.randomUUID();
        String opId = uuid.toString();
        switchContext(opId);
        return opId;
    }
}
