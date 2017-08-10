package com.kloia.eventapis.pojos;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 25/04/2017.
 */
@Data
public class PublishedEventWrapper implements Serializable {

    private static final long serialVersionUID = 7950670808405003425L;
    private String event;
    private String opId;
    private Map<String,String> userContext;

    public PublishedEventWrapper() {
    }

    public PublishedEventWrapper(String opId, String event) {
        this.opId = opId;
        this.event = event;
    }



}
