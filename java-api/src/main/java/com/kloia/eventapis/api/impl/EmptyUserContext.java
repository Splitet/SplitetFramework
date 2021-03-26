package com.kloia.eventapis.api.impl;

import com.kloia.eventapis.api.IUserContext;

import java.util.Map;

public class EmptyUserContext implements IUserContext {

    @Override
    public Map<String, String> getUserContext() {
        return null;
    }

    @Override
    public void extractUserContext(Map<String, String> userContext) {

    }

    @Override
    public void clearUserContext() {

    }

    @Override
    public String getAuditInfo() {
        return null;
    }

}
