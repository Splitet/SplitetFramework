package com.kloia.eventapis.api;

import com.kloia.eventapis.common.PublishedEvent;

/**
 * Created by zeldalozdemir on 21/02/2017.
 */
public interface RollbackSpec<P extends PublishedEvent> {
    void rollback(P event);
}