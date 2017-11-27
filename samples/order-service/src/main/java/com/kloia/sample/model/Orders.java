package com.kloia.sample.model;

import com.kloia.eventapis.spring.model.JpaEntity;
import com.kloia.eventapis.view.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Id;

/**
 * Created by zeldalozdemir on 17/02/2017.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@javax.persistence.Entity(name = "ORDERS")
public class Orders extends JpaEntity {
    private long price;
    private String stockId;
    private int orderAmount ;
    private String paymentAddress;
    private float amount;
    private String cardInformation;
    private String paymentId;
    private String address;
    private String description;
    private OrderState state;
}
