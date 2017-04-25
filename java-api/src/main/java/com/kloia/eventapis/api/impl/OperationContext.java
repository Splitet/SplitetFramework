package com.kloia.eventapis.api.impl;

import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Created by zeldalozdemir on 20/04/2017.
 */
@Repository
public class OperationContext {
    ThreadLocal<UUID> operationContext = new ThreadLocal<UUID>(){
        @Override
        protected UUID initialValue() {
            return super.initialValue();
        }
    };
    public void switchContext(UUID opid){
        operationContext.set(opid);
    }

    public UUID  getContext(){
        return operationContext.get();
    }
    public void clearContext() {
        operationContext.remove();
    }
}
