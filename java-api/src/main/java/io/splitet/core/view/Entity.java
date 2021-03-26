package io.splitet.core.view;

import io.splitet.core.common.EventKey;

import java.io.Serializable;

/**
 * Created by zeldalozdemir on 21/02/2017.
 */
public interface Entity extends Serializable {

    EventKey getEventKey();

    String getId();

    void setId(String id);

    int getVersion();

    void setVersion(int version);
}
