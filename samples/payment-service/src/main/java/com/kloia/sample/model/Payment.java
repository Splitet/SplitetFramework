package com.kloia.sample.model;

import com.kloia.eventapis.view.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;

/**
 * Created by zeldalozdemir on 31/01/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@javax.persistence.Entity(name = "PAYMENT")
public class Payment extends Entity {
    @Id
    public String getId() {
        return super.getId();
    }

    @Id
    public void setId(String id) {
        super.setId(id);
    }
    private String paymentAddress;
    private float amount;
    private String cardInformation;
    private PaymentState state;
}
