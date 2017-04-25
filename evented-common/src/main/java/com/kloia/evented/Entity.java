package com.kloia.evented;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 21/02/2017.
 */
@Data
public abstract class Entity implements Serializable {
    UUID id;
    long version;
}
