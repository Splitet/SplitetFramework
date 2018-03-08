package com.kloia.eventapis.common;


import com.kloia.eventapis.exception.EventContextException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import javax.annotation.Nullable;
import java.util.Stack;
import java.util.UUID;


public class OperationContext {

    public static final String OP_ID = "opId";
    public static final String OP_ID_HEADER = "X-opId";
    public static final String PARENT_OP_ID_HEADER = "X-parent-opId";
    public static final char PARENT_OP_ID_DELIMITER = ',';

    private ThreadLocal<Stack<Context>> operationContext = ThreadLocal.withInitial(Stack::new);

    private Context getOrCreateContext() {
        Stack<Context> contexts = operationContext.get();
        if (contexts.empty())
            contexts.push(new Context());
        return contexts.peek();
    }

    public void switchContext(String opId) {
        getOrCreateContext().setOpId(opId);
        MDC.put(OP_ID, opId);
    }

    private void switchContext(String opId, String parentOpId, boolean preGenerated) {
        Context context = getOrCreateContext();
        context.setOpId(opId);
        context.setParentOpId(parentOpId);
        context.setPreGenerated(preGenerated);
        MDC.put(OP_ID, opId);
    }

    public String getContextOpId() {
        return operationContext.get().isEmpty() ? null : operationContext.get().peek().getOpId();
    }

    public String getContextParentOpId() {
        return operationContext.get().isEmpty() ? null : operationContext.get().peek().getParentOpId();
    }

    public Context getContext() {
        return operationContext.get().isEmpty() ? null : operationContext.get().peek();
    }

    public String getCommandContext() {
        return operationContext.get().isEmpty() ? null : operationContext.get().peek().getCommandContext();

    }

    public void setCommandContext(String eventId) throws EventContextException {
        Stack<Context> context = operationContext.get();
        if (context.isEmpty()) {
            throw new EventContextException("There is no Operation Context");
        }
        context.peek().setCommandContext(eventId);
        MDC.put("command", eventId);
    }

    public void clearContext() {
        operationContext.remove();
    }

    public String clearCommandContext() {
        return operationContext.get().empty() ? null : operationContext.get().pop().getOpId();
    }

    public String generateContext() {
        return generateContext(false);
    }

    public String generateContext(boolean preGenerated) {
        return generateContext(null, preGenerated);
    }

    public String generateContext(@Nullable String parentOpId, boolean preGenerated) {
        if (operationContext.get().size() > 1)
            throw new IllegalStateException("There is Already Parent Context: " + operationContext.get().toString());
        String opId = generateOpId();
        switchContext(opId, parentOpId, preGenerated);
        return opId;
    }

    private String generateOpId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public String pushNewContext() {
        if (!operationContext.get().isEmpty()) {
            Context peek = operationContext.get().peek();
            if (peek.isPreGenerated()) {
                peek.setPreGenerated(false);
                return peek.getOpId();
            } else {
                String opId = generateOpId();
                String parentOpId = StringUtils.isEmpty(peek.getParentOpId()) ? peek.getOpId() + PARENT_OP_ID_DELIMITER + peek.getParentOpId() : peek.getOpId();
                return pushContext(opId, parentOpId).getOpId();
            }
        } else
            return pushContext(generateOpId()).getOpId();
    }

    private Context pushContext(String opId) {
        return pushContext(opId, null);
    }

    private Context pushContext(String opId, String parentOpId) {
        Context context = operationContext.get().push(new Context(opId));
        context.setParentOpId(parentOpId);
        MDC.put(OP_ID, opId);
        return context;
    }

}
