package com.kloia.eventapis.kafka;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kloia.eventapis.common.Context;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by zeldalozdemir on 25/04/2017.
 */
@Data
@ToString(exclude = "userContext")
public class PublishedEventWrapper implements Serializable {

    private static final long serialVersionUID = 7950670808405003425L;

    @JsonIgnore
    private String event;
    private Context context;
    private String sender;
    private long opDate;
    private Map<String, String> userContext;

    public PublishedEventWrapper() {
    }

    public PublishedEventWrapper(Context context, String eventData, long opDate) {
        this.context = context;
        this.event = eventData;
        this.opDate = opDate;
    }

    @JsonGetter
    @JsonRawValue
    public String getEvent() {
        return event;
    }

    @JsonSetter
    public void setEvent(ObjectNode event) {
        this.event = event.toString();
    }

    @JsonSetter
    public void setEventData(String eventData) {
        this.event = eventData;
    }


}
