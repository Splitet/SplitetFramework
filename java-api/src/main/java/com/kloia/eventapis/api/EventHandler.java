package com.kloia.eventapis.api;

/**
 * Created by zeldalozdemir on 21/04/2017.
 */
public interface EventHandler<D> {
    Object execute(D dto) throws Exception ;
}
