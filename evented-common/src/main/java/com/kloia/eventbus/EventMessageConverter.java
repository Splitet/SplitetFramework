package com.kloia.eventbus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.kloia.eventapis.api.impl.OperationContext;
import com.kloia.eventapis.pojos.PublishedEventWrapper;
import com.kloia.evented.IUserContext;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.converter.MessagingMessageConverter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

/**
 * Created by zeldalozdemir on 25/04/2017.
 */
@Component
public class EventMessageConverter extends MessagingMessageConverter {
    private final ObjectMapper objectMapper;
    private OperationContext operationContext;
    private IUserContext userContext;


    @Autowired
    public EventMessageConverter(ObjectMapper objectMapper, OperationContext operationContext, IUserContext userContext) {
        this.objectMapper = objectMapper;
        this.operationContext = operationContext;
        this.userContext = userContext;
    }

    @Override
    protected Object extractAndConvertValue(ConsumerRecord<?, ?> record, Type type) {
        Object value = record.value();
        if (value instanceof PublishedEventWrapper)
            try {
                PublishedEventWrapper eventWrapper = (PublishedEventWrapper) value;
                userContext.extractUserContext(eventWrapper.getUserContext());
                operationContext.switchContext(eventWrapper.getOpId());
                return objectMapper.treeToValue(eventWrapper.getEvent(), TypeFactory.rawClass(type));
            } catch (JsonProcessingException e) {
                throw new SerializationException(e);
            }
        else
            return super.extractAndConvertValue(record, type);
    }
}
