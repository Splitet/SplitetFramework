package com.kloia.eventapis.common;


import com.kloia.eventapis.api.Views;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zeldalozdemir on 21/04/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class PublishedEvent{

    @JsonView(Views.PublishedOnly.class)
    EventKey sender;

    public abstract EventType getEventType();
}
