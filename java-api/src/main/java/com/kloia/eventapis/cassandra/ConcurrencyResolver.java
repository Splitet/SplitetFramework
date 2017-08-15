package com.kloia.eventapis.cassandra;

public interface ConcurrencyResolver {
    boolean tryMore();
}
