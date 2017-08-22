package com.kloia.eventapis.spring.model;

import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.view.Entity;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@Data
@MappedSuperclass
public abstract class JpaEntity implements Entity {

    @Id
    private String id;
    private int version;

    public JpaEntity() {
    }

    public JpaEntity(String id, int version) {
        this.id = id;
        this.version = version;
    }

    @Override
    public EventKey getEventKey() {
        return new EventKey(id, version);
    }

}
