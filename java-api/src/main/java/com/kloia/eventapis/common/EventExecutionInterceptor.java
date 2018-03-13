package com.kloia.eventapis.common;


import com.kloia.eventapis.kafka.KafkaOperationRepository;
import com.kloia.eventapis.pojos.EventState;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * Created by zeldalozdemir on 24/04/2017.
 */
@Aspect
@Slf4j
@SuppressWarnings("checkstyle:IllegalThrows")
public class EventExecutionInterceptor {

    private KafkaOperationRepository kafkaOperationRepository;
    private OperationContext operationContext;

    public EventExecutionInterceptor(KafkaOperationRepository kafkaOperationRepository, OperationContext operationContext) {
        this.operationContext = operationContext;
        this.kafkaOperationRepository = kafkaOperationRepository;
    }

    @Before("this(com.kloia.eventapis.api.EventHandler+) && execution(* execute(..)) && args(object)")
    public void before(JoinPoint jp, Object object) throws Throwable {
        String commandContext = object == null ? jp.getTarget().getClass().getSimpleName() : object.getClass().getSimpleName();
        operationContext.setCommandContext(commandContext);
        log.debug("before method:" + (object == null ? "" : object.toString()));
    }

    @AfterReturning(value = "this(com.kloia.eventapis.api.EventHandler+) && execution(* execute(..))", returning = "retVal")
    public void afterReturning(Object retVal) throws Throwable {
        log.debug("AfterReturning:" + (retVal == null ? "" : retVal.toString()));
        operationContext.clearCommandContext();
    }

    @AfterThrowing(value = "this(com.kloia.eventapis.api.EventHandler+) && execution(* execute(..))", throwing = "exception")
    public void afterThrowing(Exception exception) throws Throwable {
        try {
            log.debug("afterThrowing EventHandler method:" + exception.getMessage());
            kafkaOperationRepository.failOperation(operationContext.getCommandContext(), event -> event.setEventState(EventState.TXN_FAILED));
        } finally {
            operationContext.clearCommandContext();
        }
    }

/*    @Around(value = " @annotation(org.springframework.kafka.annotation.KafkaListener))")
    public Object aroundListen(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        log.info("Event Here:"+proceedingJoinPoint.getArgs()[0].toString());
        return proceedingJoinPoint.proceed();
    }*/

}