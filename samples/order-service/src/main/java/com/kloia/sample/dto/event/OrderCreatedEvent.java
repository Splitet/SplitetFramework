package com.kloia.sample.dto.event;

import com.fasterxml.jackson.annotation.JsonView;
import com.kloia.eventapis.pojos.PublishedEvent;
import com.kloia.eventapis.pojos.Views;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent extends PublishedEvent {
    private String stockId;
    private int orderAmount ;
    private String description;
}
