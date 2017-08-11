package com.kloia.eventapis.api;

/**
 * Created by zeldalozdemir on 21/04/2017.
 */
public interface CommandHandler<E, D> {
    Object execute(D dto) throws Exception;
}
