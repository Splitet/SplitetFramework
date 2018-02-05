package com.kloia.eventapis.common;


import com.kloia.eventapis.exception.EventContextException;
import org.slf4j.MDC;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;


public class OperationContext {

    private ThreadLocal<Map.Entry<String, Stack<String>>> operationContext = new ThreadLocal<Map.Entry<String, Stack<String>>>() {
        @Override
        protected Map.Entry<String, Stack<String>> initialValue() {
            return super.initialValue();
        }
    };

    public void switchContext(String opId) {
        operationContext.set(new AbstractMap.SimpleEntry<>(opId, new Stack<>()));
        MDC.put("opId", opId);
    }

    public String getContext() {
        Map.Entry<String, Stack<String>> entry = operationContext.get();
        return entry == null ? null : entry.getKey();
    }

    public String getCommandContext() {
        Map.Entry<String, Stack<String>> entry = operationContext.get();
        return entry == null ? null : entry.getValue().peek();
    }

    public void setCommandContext(String eventId) throws EventContextException {
        Map.Entry<String, Stack<String>> entry = operationContext.get();
        if (entry == null) {
            throw new EventContextException("There is no Operation Context");
        }
        entry.getValue().push(eventId);
        MDC.put("command",eventId);
    }

    public void clearContext() {
        operationContext.remove();
    }

    public String clearCommandContext() {
        Map.Entry<String, Stack<String>> entry = operationContext.get();
        String pop = entry.getValue().pop();
        if(entry.getValue().empty())
            operationContext.remove();
        return pop;
    }

    public String generateContext() {
        UUID uuid = UUID.randomUUID();
        String opId = uuid.toString();
        switchContext(opId);
        return opId;
    }
}
