package com.kloia.eventapis.api;

/**
 * Created by zeldalozdemir on 21/04/2017.
 */
public interface CommandHandler<D extends CommandDto> {

    long DEFAULT_COMMAND_TIMEOUT = 10000L;

    default long getCommandTimeout() {
        return DEFAULT_COMMAND_TIMEOUT;
    }

    EventRepository getDefaultEventRepository();

    Object execute(D dto) throws Exception;
}
