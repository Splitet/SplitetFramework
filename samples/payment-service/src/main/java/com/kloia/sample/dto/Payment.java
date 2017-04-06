package com.kloia.sample.dto;

import com.kloia.evented.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zeldalozdemir on 31/01/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment extends Entity {
    private long paymentId;
    private String paymentAddress;
    private float amount;
    private String cardInformation;
    private String state;
}
