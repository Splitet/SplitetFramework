package com.kloia.evented;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 21/02/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class Entity implements Serializable {
    protected UUID id;
    protected Long version;
}
