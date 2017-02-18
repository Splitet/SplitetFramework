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
public class OrderProcessAggDTO {
    private long orderId;
    private long price;
    private int orderAmount ;
    private String address;
    private String description;
    private PaymentProcessAggDTO paymentProcessAggDTO;
}
