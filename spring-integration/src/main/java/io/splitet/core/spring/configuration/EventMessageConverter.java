package io.splitet.core.spring.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.splitet.core.api.IUserContext;
import io.splitet.core.common.Context;
import io.splitet.core.common.OperationContext;
import io.splitet.core.kafka.IOperationRepository;
import io.splitet.core.kafka.PublishedEventWrapper;
import io.splitet.core.pojos.EventState;
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
    private IOperationRepository operationRepository;


    public EventMessageConverter(ObjectMapper objectMapper, OperationContext operationContext, IUserContext userContext, IOperationRepository operationRepository) {
        this.objectMapper = objectMapper;
        this.operationContext = operationContext;
        this.userContext = userContext;
        this.operationRepository = operationRepository;
    }

    @Override
    public Object extractAndConvertValue(ConsumerRecord<?, ?> record, Type type) {
        Object value = record.value();
        if (value instanceof PublishedEventWrapper) {
            PublishedEventWrapper eventWrapper = (PublishedEventWrapper) value;
            Context context = eventWrapper.getContext();
            context.setCommandContext(record.topic());
            operationContext.switchContext(context);
            userContext.extractUserContext(eventWrapper.getUserContext());
            try {
                return objectMapper.readValue(eventWrapper.getEvent(), TypeFactory.rawClass(type));
            } catch (IOException e) {
                operationRepository.failOperation(operationContext.getCommandContext(), event -> event.setEventState(EventState.TXN_FAILED));
                throw new SerializationException(e);
            }
        } else
            return super.extractAndConvertValue(record, type);
    }
}
