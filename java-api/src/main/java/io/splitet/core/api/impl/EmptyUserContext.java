package io.splitet.core.api.impl;

import io.splitet.core.api.IUserContext;

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
