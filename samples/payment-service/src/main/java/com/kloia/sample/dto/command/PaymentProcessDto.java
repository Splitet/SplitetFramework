package com.kloia.sample.dto.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zeldalozdemir on 31/01/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentProcessDto {
    private String orderId;
    private String paymentAddress;
    private float amount;
    private String cardInformation;

}
