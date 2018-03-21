package com.kloia.eventapis.spring.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.kloia.eventapis.api.IUserContext;
import com.kloia.eventapis.common.Context;
import com.kloia.eventapis.common.OperationContext;
import com.kloia.eventapis.kafka.PublishedEventWrapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.springframework.kafka.support.converter.MessagingMessageConverter;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Created by zeldalozdemir on 25/04/2017.
 */
public class EventMessageConverter extends MessagingMessageConverter {
    private final ObjectMapper objectMapper;
    private OperationContext operationContext;
    private IUserContext userContext;


    public EventMessageConverter(ObjectMapper objectMapper, OperationContext operationContext, IUserContext userContext) {
        this.objectMapper = objectMapper;
        this.operationContext = operationContext;
        this.userContext = userContext;
    }

    @Override
    public Object extractAndConvertValue(ConsumerRecord<?, ?> record, Type type) {
        Object value = record.value();
        if (value instanceof PublishedEventWrapper)
            try {
                PublishedEventWrapper eventWrapper = (PublishedEventWrapper) value;
                Context context = eventWrapper.getContext();
                context.setCommandContext(record.topic());
                operationContext.switchContext(context);
                userContext.extractUserContext(eventWrapper.getUserContext());
                return objectMapper.readValue(eventWrapper.getEvent(), TypeFactory.rawClass(type));
            } catch (IOException e) {
                throw new SerializationException(e);
            }
        else
            return super.extractAndConvertValue(record, type);
    }
}
