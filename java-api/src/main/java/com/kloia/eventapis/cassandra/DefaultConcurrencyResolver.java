package com.kloia.eventapis.cassandra;

public class DefaultConcurrencyResolver implements ConcurrencyResolver {
    @Override
    public boolean tryMore() {
        return false;
    }
}
