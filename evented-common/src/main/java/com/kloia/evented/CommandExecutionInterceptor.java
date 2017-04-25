package com.kloia.evented;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Created by zeldalozdemir on 24/04/2017.
 */
@Aspect
public class CommandExecutionInterceptor {

    @Before("within(Command)")
    public Object invoke(MethodInvocation invocation) throws Throwable {
        System.out.println("before method");
        Object retVal = invocation.proceed();
        System.out.println("after method");
        return retVal;
    }
}