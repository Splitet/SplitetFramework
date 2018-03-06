package com.kloia.eventapis.common;


import com.fasterxml.jackson.annotation.JsonView;
import com.kloia.eventapis.api.Views;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zeldalozdemir on 21/04/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class PublishableEvent {

    @JsonView(Views.PublishedOnly.class)
    EventKey sender;

    public abstract EventType getEventType();
}
