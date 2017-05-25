package com.kloia.evented;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 21/02/2017.
 */
@Data

public abstract class Entity implements Serializable {
    public Entity() {
    }

    public Entity(String id, Long version) {
        this.id = id;
        this.version = version;
    }
    public Entity(UUID id, Long version) {
        this.id = id.toString();
        this.version = version;
    }
    protected String id;
    protected Long version;
}
