package com.kloia.sample.dto.event;

import com.kloia.eventapis.pojos.PublishedEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zeldalozdemir on 31/01/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPaidEvent extends PublishedEvent{
    private String paymentId;

}
