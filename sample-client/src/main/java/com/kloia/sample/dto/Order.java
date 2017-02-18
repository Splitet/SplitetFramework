package com.kloia.sample.dto;

import lombok.Data;

/**
 * Created by zeldalozdemir on 17/02/2017.
 */
@Data
public class Order {
    private long orderId;
    private long price;
    private int orderAmount ;
    private String address;
    private String description;
}
