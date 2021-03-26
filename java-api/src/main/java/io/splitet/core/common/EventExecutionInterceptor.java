package io.splitet.core.common;


import io.splitet.core.api.IUserContext;
import io.splitet.core.kafka.KafkaOperationRepository;
import io.splitet.core.pojos.EventState;
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

/*    @Before("this(EventHandler+) && execution(* execute(..)) && args(object)")
    public void before(JoinPoint jp, Object object) throws Throwable {
        String commandContext = object == null ? jp.getTarget().getClass().getSimpleName() : object.getClass().getSimpleName();
        operationContext.setCommandContext(commandContext);
        log.debug("before method:" + (object == null ? "" : object.toString()));
    }*/

    @AfterReturning(value = "this(io.splitet.core.api.EventHandler+) && execution(* execute(..))", returning = "retVal")
    public void afterReturning(Object retVal) throws Throwable {
        log.debug("AfterReturning:" + (retVal == null ? "" : retVal.toString()));
        operationContext.clearCommandContext();
        userContext.clearUserContext();
    }

    @AfterThrowing(value = "this(io.splitet.core.api.EventHandler+) && execution(* execute(..))", throwing = "exception")
    public void afterThrowing(Exception exception) throws Throwable {
        try {
            log.debug("afterThrowing EventHandler method:" + exception.getMessage());
            kafkaOperationRepository.failOperation(operationContext.getCommandContext(), event -> event.setEventState(EventState.TXN_FAILED));
        } finally {
            operationContext.clearCommandContext();
            userContext.clearUserContext();
        }
    }

/*    @Around(value = " @annotation(org.springframework.kafka.annotation.KafkaListener))")
    public Object aroundListen(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        log.info("Event Here:"+proceedingJoinPoint.getArgs()[0].toString());
        return proceedingJoinPoint.proceed();
    }*/

}