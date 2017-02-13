package com.kloia.sample.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zeldalozdemir on 31/01/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreateAggDTO {
    private long orderId;
    private String paymentCard;
    private long price;
    private String address;
    private String Description;
}
