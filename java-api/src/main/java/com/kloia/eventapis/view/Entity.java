package com.kloia.eventapis.view;

import com.kloia.eventapis.common.EventKey;

import java.io.Serializable;

/**
 * Created by zeldalozdemir on 21/02/2017.
 */
public interface Entity extends Serializable {

    public EventKey getEventKey();

    public String getId();

    public void setId(String id);

    public int getVersion();

    public void setVersion(int version);
}
