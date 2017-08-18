package com.kloia.eventapis.view;

import com.google.common.reflect.TypeToken;
import com.kloia.eventapis.common.PublishedEvent;
import lombok.Getter;
import lombok.NonNull;

import java.lang.reflect.ParameterizedType;

/**
 * Created by zeldalozdemir on 21/02/2017.
 */
public interface RollbackSpec<P extends PublishedEvent> {
    void rollback(P event);
}