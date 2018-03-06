package com.kloia.eventapis.api;

import com.kloia.eventapis.common.PublishableEvent;

/**
 * Created by zeldalozdemir on 21/02/2017.
 */
public interface RollbackSpec<P extends PublishableEvent> {
    void rollback(P event);
}