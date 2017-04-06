package com.kloia.sample.dto;

import com.kloia.evented.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zeldalozdemir on 17/02/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order extends Entity {
    private long orderId;
    private long price;
    private int orderAmount ;
    private String address;
    private String description;
    private String state;
}
