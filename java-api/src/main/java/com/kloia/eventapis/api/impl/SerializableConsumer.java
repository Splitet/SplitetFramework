package com.kloia.eventapis.api.impl;

/**
 * Created by zeldalozdemir on 22/02/2017.
 */

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Consumer;
@FunctionalInterface
public interface SerializableConsumer<T>  extends Consumer<T>, Serializable{

}