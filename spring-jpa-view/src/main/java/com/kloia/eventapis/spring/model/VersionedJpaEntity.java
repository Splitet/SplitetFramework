package com.kloia.eventapis.spring.model;

import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.view.Entity;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

@Data
@MappedSuperclass
public abstract class VersionedJpaEntity implements Entity {

    @Id
    protected String id;
    @Version
    protected int version;

    public VersionedJpaEntity() {
    }

    public VersionedJpaEntity(String id, int version) {
        this.id = id;
        this.version = version;
    }

    @Override
    public EventKey getEventKey() {
        return new EventKey(id, version);
    }

}
