package com.kloia.eventapis.api;

import java.util.Map;

public interface IUserContext {
    Map<String,String> getUserContext();

    void extractUserContext(Map<String, String> userContext);
}
