package com.kloia.evented;

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
