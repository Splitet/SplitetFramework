package com.kloia.eventapis.common;


import com.kloia.eventapis.api.Command;
import com.kloia.eventapis.api.CommandDto;
import com.kloia.eventapis.api.CommandHandler;
import com.kloia.eventapis.api.EventRepository;
import com.kloia.eventapis.cassandra.ConcurrentEventException;
import com.kloia.eventapis.cassandra.DefaultConcurrencyResolver;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.kafka.KafkaOperationRepository;
import com.kloia.eventapis.pojos.CommandRecord;
import com.kloia.eventapis.pojos.EventState;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * Created by zeldalozdemir on 24/04/2017.
 */
@Aspect
@Slf4j
@SuppressWarnings("checkstyle:IllegalThrows")
public class CommandExecutionInterceptor {

    private KafkaOperationRepository kafkaOperationRepository;
    private OperationContext operationContext;

    public CommandExecutionInterceptor(KafkaOperationRepository kafkaOperationRepository, OperationContext operationContext) {
        this.operationContext = operationContext;
        this.kafkaOperationRepository = kafkaOperationRepository;
    }


    @Before("this(com.kloia.eventapis.api.CommandHandler) && @annotation(command)")
    public void before(JoinPoint jp, Command command) throws Throwable {
        Object target = jp.getTarget();
        if (!(target instanceof CommandHandler))
            throw new IllegalArgumentException("Point is not Instance of CommandHandler");
        CommandHandler commandHandler = (CommandHandler) target;
        long commandTimeout = command.commandTimeout();
        operationContext.startNewContext(commandTimeout); // Ability to generate new Context
        operationContext.setCommandContext(target.getClass().getSimpleName());
        CommandRecord commandDto = recordCommand(jp, commandHandler, command);
        log.debug("before method:" + (commandDto == null ? "" : commandDto.toString()));
    }

    private CommandRecord recordCommand(JoinPoint jp, CommandHandler commandHandler, Command command) throws ConcurrentEventException, EventStoreException {
        EventRepository eventRepository;
        CommandDto commandDto = null;
        CommandRecord commandRecord = new CommandRecord();
        commandRecord.setEventName(commandHandler.getClass().getSimpleName());
        for (int i = 0; i < jp.getArgs().length; i++) {
            Object arg = jp.getArgs()[i];
            commandRecord.getParameters().put(i, arg);
        }
//        for (Object arg : jp.getArgs()) {
//            if (arg instanceof CommandDto)
//                commandDto = (CommandDto) arg;
//        }
//        if (commandDto == null) {
//            log.warn("Command" + jp.getTarget().getClass().getSimpleName() + " does not have CommandDto");
//            return null;
//        }
        try {
            Field declaredField = commandHandler.getClass().getDeclaredField(command.eventRepository());
            if (!declaredField.isAccessible())
                declaredField.setAccessible(true);
            eventRepository = (EventRepository) declaredField.get(commandHandler);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            log.error("Error while accessing EventRecorder(" + command.eventRepository() + ") of Command:" + commandHandler.getClass().getSimpleName() + " message: " + e.getMessage());
            return null;
        }
        if (eventRepository != null) {
            eventRepository.getEventRecorder().recordEntityEvent(commandRecord, System.currentTimeMillis(), Optional.empty(), entityEvent -> new DefaultConcurrencyResolver());
        } else
            log.error("Error while accessing EventRecorder(" + command.eventRepository() + " is null ) of Command:" + commandHandler.getClass().getSimpleName());
        return commandRecord;
    }

    @AfterReturning(value = "this(com.kloia.eventapis.api.CommandHandler) && @annotation(command)", returning = "retVal")
    public void afterReturning(Command command, Object retVal) {
        log.debug("AfterReturning:" + (retVal == null ? "" : retVal.toString()));
        operationContext.clearCommandContext();
    }

    @AfterThrowing(value = "this(com.kloia.eventapis.api.CommandHandler) && @annotation(command)", throwing = "exception")
    public void afterThrowing(Command command, Exception exception) {
        try {
            log.info("afterThrowing Command: " + exception);
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