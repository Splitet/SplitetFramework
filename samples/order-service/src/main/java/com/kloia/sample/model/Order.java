package com.kloia.sample.model;

import com.kloia.eventapis.view.Entity;
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
    private long price;
    private String stockId;
    private int orderAmount ;
    private PaymentInformation paymentInformation;
    private String paymentId;
    private String address;
    private String description;
    private OrderState state;
}
