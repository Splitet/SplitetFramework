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
public class EventExecutionInterceptor {

    private KafkaOperationRepository kafkaOperationRepository;
    private OperationContext operationContext;

    public EventExecutionInterceptor(KafkaOperationRepository kafkaOperationRepository, OperationContext operationContext) {
        this.operationContext = operationContext;
        this.kafkaOperationRepository = kafkaOperationRepository;
    }

    @Before("within(com.kloia.eventapis.api.EventHandler+) && execution(* execute(..)) && args(object)")
    public void before(JoinPoint jp, Object object) throws Throwable {
        String commandContext = object == null ? jp.getTarget().getClass().getSimpleName() : object.getClass().getSimpleName();
        operationContext.setCommandContext(commandContext);
        log.info("before method:" + (object == null ? "" : object.toString()));
    }

    @AfterReturning(value = "within(com.kloia.eventapis.api.EventHandler+) && execution(* execute(..))", returning = "retVal")
    public void afterReturning(Object retVal) throws Throwable {
//        kafkaOperationRepository.updateEvent(operationContext.getContext(),operationContext.clearCommandContext(),event -> event.setEventState(EventState.SUCCEDEED));
        log.info("AfterReturning:" + (retVal == null ? "" : retVal.toString()));
        operationContext.clearCommandContext();
    }

    @AfterThrowing(value = "within(com.kloia.eventapis.api.EventHandler+) && execution(* execute(..))", throwing = "e")
    public void afterThrowing(Exception e) throws Throwable {
        try {
            log.info("afterThrowing method:" + e);
            kafkaOperationRepository.failOperation(operationContext.getContext(), operationContext.getCommandContext(), event -> event.setEventState(EventState.TXN_FAILED));
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