package com.kloia.eventapis.api;

import com.fasterxml.jackson.annotation.JacksonAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Command Entry Methods.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface Command {
    long commandTimeout() default CommandHandler.DEFAULT_COMMAND_TIMEOUT;

    String eventRepository() default "eventRepository";

}
