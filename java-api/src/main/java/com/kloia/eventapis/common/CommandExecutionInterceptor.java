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
public class CommandExecutionInterceptor {

    private KafkaOperationRepository kafkaOperationRepository;
    private OperationContext operationContext;

    public CommandExecutionInterceptor(KafkaOperationRepository kafkaOperationRepository, OperationContext operationContext) {
        this.operationContext = operationContext;
        this.kafkaOperationRepository = kafkaOperationRepository;
    }

    @Before("within(com.kloia.eventapis.api.CommandHandler+) && execution(public * *(..)) && args(object,..)")
    public void before(JoinPoint jp, Object object) throws Throwable {
        operationContext.pushNewContext(); // Ability to generate new Context
        operationContext.setCommandContext(jp.getTarget().getClass().getSimpleName());
        log.debug("before method:" + (object == null ? "" : object.toString()));
    }

    @AfterReturning(value = "within(com.kloia.eventapis.api.CommandHandler+) && execution(public * *(..))", returning = "retVal")
    public void afterReturning(Object retVal) {
        log.debug("AfterReturning:" + (retVal == null ? "" : retVal.toString()));
        operationContext.clearCommandContext();
    }

    @AfterThrowing(value = "within(com.kloia.eventapis.api.CommandHandler+) && execution(public * *(..))", throwing = "e")
    public void afterThrowing(Exception e) {
        try {
            log.info("afterThrowing Command: " + e);
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