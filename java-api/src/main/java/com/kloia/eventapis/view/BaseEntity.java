package com.kloia.eventapis.view;

import com.kloia.eventapis.common.EventKey;
import lombok.Data;

@Data
public abstract class BaseEntity implements Entity {

    private String id;
    private int version;

    public BaseEntity() {
    }

    public BaseEntity(String id, int version) {
        this.id = id;
        this.version = version;
    }

    @Override
    public EventKey getEventKey() {
        return new EventKey(id, version);
    }

}
