package com.kloia.eventapis.view;

import com.kloia.eventapis.common.EventKey;
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

    public Entity(String id, int version) {
        this.id = id;
        this.version = version;
    }
    public Entity(UUID id, int version) {
        this.id = id.toString();
        this.version = version;
    }
    protected String id;
    protected int version;
    public EventKey getEventKey(){
        return new EventKey(id,version);
    }
}
