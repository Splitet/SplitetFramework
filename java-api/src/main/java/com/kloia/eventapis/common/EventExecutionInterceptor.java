package com.kloia.eventapis.common;


import com.kloia.eventapis.api.IUserContext;
import com.kloia.eventapis.kafka.KafkaOperationRepository;
import com.kloia.eventapis.pojos.EventState;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;

/**
 * Created by zeldalozdemir on 24/04/2017.
 */
@Aspect
@Slf4j
@SuppressWarnings("checkstyle:IllegalThrows")
public class EventExecutionInterceptor {

    private KafkaOperationRepository kafkaOperationRepository;
    private OperationContext operationContext;
    private IUserContext userContext;

    public EventExecutionInterceptor(KafkaOperationRepository kafkaOperationRepository, OperationContext operationContext, IUserContext userContext) {
        this.operationContext = operationContext;
        this.kafkaOperationRepository = kafkaOperationRepository;
        this.userContext = userContext;
    }

    @AfterReturning(value = "this(com.kloia.eventapis.api.EventHandler+) && execution(* execute(..))", returning = "retVal")
    public void afterReturning(Object retVal) throws Throwable {
        log.debug("AfterReturning:" + (retVal == null ? "" : retVal.toString()));
        operationContext.clearCommandContext();
        userContext.clearUserContext();
    }

    @AfterThrowing(value = "this(com.kloia.eventapis.api.EventHandler+) && execution(* execute(..))", throwing = "exception")
    public void afterThrowing(Exception exception) throws Throwable {
        try {
            log.debug("afterThrowing EventHandler method:" + exception.getMessage());
            kafkaOperationRepository.failOperation(operationContext.getCommandContext(), event -> event.setEventState(EventState.TXN_FAILED));
        } finally {
            operationContext.clearCommandContext();
            userContext.clearUserContext();
        }
    }
}