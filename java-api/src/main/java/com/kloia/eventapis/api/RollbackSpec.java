package com.kloia.eventapis.api;

import com.kloia.eventapis.common.RecordedEvent;

/**
 * Created by zeldalozdemir on 21/02/2017.
 */
public interface RollbackSpec<P extends RecordedEvent> {
    void rollback(P event);
}