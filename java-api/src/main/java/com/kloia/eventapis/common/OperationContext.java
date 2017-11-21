package com.kloia.eventapis.common;


import com.kloia.eventapis.exception.EventContextException;
import org.slf4j.MDC;

import java.util.AbstractMap;
import java.util.Map;
import java.util.UUID;


public class OperationContext {

    private ThreadLocal<Map.Entry<String, String>> operationContext = new ThreadLocal<Map.Entry<String, String>>() {
        @Override
        protected Map.Entry<String, String> initialValue() {
            return super.initialValue();
        }
    };

    public void switchContext(String opId) {
        operationContext.set(new AbstractMap.SimpleEntry<>(opId, null));
        MDC.put("opId", opId);
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
        if (entry == null) {
            throw new EventContextException("There is no Operation Context");
        }
        entry.setValue(eventId);
        MDC.put("command",eventId);
    }

    public void clearContext() {
        operationContext.remove();
        MDC.remove("session");
        MDC.remove("opId");
        MDC.remove("command");
    }

    public String clearCommandContext() {
        Map.Entry<String, String> entry = operationContext.get();
        if (entry != null) {
            return entry.setValue(null);
        }
        return null;
    }

    public String generateContext() {
        UUID uuid = UUID.randomUUID();
        String opId = uuid.toString();
        switchContext(opId);
        return opId;
    }
}
