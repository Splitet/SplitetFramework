package io.splitet.core.common;


import io.splitet.core.exception.EventContextException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import javax.annotation.Nullable;
import java.util.Stack;
import java.util.UUID;


public class OperationContext {

    public static final String OP_ID = "opId";
    public static final String COMMAND = "command";
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

    public void switchContext(Context context) {
        operationContext.get().push(context);
        MDC.put(OP_ID, context.getOpId());
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
        MDC.put(COMMAND, eventId);
    }

    public void clearContext() {
        operationContext.remove();
        MDC.remove(COMMAND);
        MDC.remove(OP_ID);
    }

    public String clearCommandContext() {
        if (operationContext.get().empty()) {
            return null;
        } else {
            MDC.remove(COMMAND);
            MDC.remove(OP_ID);
            Context pop = operationContext.get().pop();
            if (!operationContext.get().isEmpty()) {
                Context newContext = operationContext.get().peek();
                MDC.put(COMMAND, newContext.getCommandContext());
                MDC.put(OP_ID, newContext.getOpId());
            }
            return pop.getOpId();
        }
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

    public String startNewContext(long commandTimeout) {
        Context peek;
        if (!operationContext.get().isEmpty()) {
            peek = operationContext.get().peek();
            if (peek.isPreGenerated()) {
                peek.setCommandTimeout(commandTimeout);
                peek.setStartTime(System.currentTimeMillis());
                peek.setGenerated();
            } else {
                String opId = generateOpId();
                String parentOpId = StringUtils.isEmpty(peek.getParentOpId()) ? peek.getOpId() + PARENT_OP_ID_DELIMITER + peek.getParentOpId() : peek.getOpId();
                peek = pushContext(opId);
                peek.setParentOpId(parentOpId);
                peek.setCommandTimeout(commandTimeout);
                peek.setStartTime(System.currentTimeMillis());
            }
        } else {
            peek = pushContext(generateOpId());
            peek.setCommandTimeout(commandTimeout);
            peek.setStartTime(System.currentTimeMillis());
        }


        return peek.getOpId();
    }


    private Context pushContext(String opId) {
        Context context = operationContext.get().push(new Context(opId));
        MDC.put(OP_ID, opId);
        return context;
    }

}
