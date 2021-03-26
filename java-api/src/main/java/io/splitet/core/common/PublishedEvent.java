package io.splitet.core.common;


import com.fasterxml.jackson.annotation.JsonView;
import io.splitet.core.api.Views;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zeldalozdemir on 21/04/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class PublishedEvent extends ReceivedEvent {

    @JsonView(Views.PublishedOnly.class)
    EventKey sender;

    public abstract EventType getEventType();
}
