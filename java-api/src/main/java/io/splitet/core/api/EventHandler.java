package io.splitet.core.api;

import io.splitet.core.common.ReceivedEvent;

/**
 * Created by zeldalozdemir on 21/04/2017.
 */
public interface EventHandler<D extends ReceivedEvent> {
    Object execute(D event) throws Exception;
}
