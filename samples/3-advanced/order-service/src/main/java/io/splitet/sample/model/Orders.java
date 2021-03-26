package io.splitet.sample.model;

import io.splitet.core.spring.model.JpaEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * Created by zeldalozdemir on 17/02/2017.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@javax.persistence.Entity(name = "ORDERS")
public class Orders extends JpaEntity {

    @Enumerated(EnumType.STRING)
    private OrderState state;

    private long price;

    private String stockId;

    private int reservedStockVersion;

    private int orderAmount;

    private String paymentAddress;

    private float amount;

    private String cardInformation;

    private String paymentId;

    private String address;

    private String description;
}
