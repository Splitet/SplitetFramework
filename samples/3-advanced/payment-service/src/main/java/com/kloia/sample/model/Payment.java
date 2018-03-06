package com.kloia.sample.model;

import com.kloia.eventapis.spring.model.JpaEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zeldalozdemir on 31/01/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@javax.persistence.Entity(name = "PAYMENT")
public class Payment extends JpaEntity {
    private String paymentAddress;
    private String orderId;
    private float amount;
    private String cardInformation;
    private PaymentState state;
}
