package com.kloia.eventapis.api.impl;

import com.kloia.eventapis.api.IdCreationStrategy;

import java.util.UUID;

/**
 * Created by zeldal on 25/05/2017.
 */
public class UUIDCreationStrategy implements IdCreationStrategy {
    @Override
    public String nextId() {
        return UUID.randomUUID().toString();
    }
}
