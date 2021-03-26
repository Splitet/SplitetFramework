package io.splitet.core.api;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeToken;
import io.splitet.core.common.RecordedEvent;
import io.splitet.core.pojos.CommandRecord;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.AbstractMap;
import java.util.Map;

/**
 * Created by zeldalozdemir on 21/02/2017.
 * To use this, You have to implement rollback method with same signature as CommandHandler.execute
 * When rollback occurs, it will trigger rollback with same parameters.
 */
public interface RollbackCommandSpec<P extends CommandHandler> extends RollbackSpec<CommandRecord> {

    default Map.Entry<String, Class<RecordedEvent>> getNameAndClass() {
        ParameterizedType type = (ParameterizedType) TypeToken.of(this.getClass().getGenericInterfaces()[0]).getType();
        try {
            Class<CommandHandler> publishedEventClass = (Class<CommandHandler>) Class.forName(type.getActualTypeArguments()[0].getTypeName());
            return new AbstractMap.SimpleEntry(publishedEventClass.getSimpleName(), CommandRecord.class);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    default void rollback(CommandRecord record) {
        ObjectMapper objectMapper = new ObjectMapper(); // Get from context
        for (Method method : this.getClass().getDeclaredMethods()) {
            if (method.getName().equals("rollback")) {
                if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == CommandRecord.class)
                    continue;
                try {
                    Object[] args = new Object[method.getParameterCount()];
                    for (Map.Entry<Integer, ?> entry : record.getParameters().entrySet()) {
                        Class<?> type = method.getParameterTypes()[entry.getKey()];
                        args[entry.getKey()] = objectMapper.treeToValue((TreeNode) entry.getValue(), type);
                    }
                    method.invoke(this, args);
                } catch (InvocationTargetException | IllegalAccessException | IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
    }
}