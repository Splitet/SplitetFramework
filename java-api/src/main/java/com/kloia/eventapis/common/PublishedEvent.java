package com.kloia.eventapis.common;


import com.kloia.eventapis.api.Views;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;

/**
 * Created by zeldalozdemir on 21/04/2017.
 */
@Data
public class PublishedEvent{
    @JsonView(Views.PublishedOnly.class)
    EventKey sender;
}
