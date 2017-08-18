package com.kloia.sample.model;

import com.kloia.eventapis.view.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
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
@javax.persistence.Entity(name = "\"ORDER\"")
public class Order extends Entity {
    @Id
    public String getId(){
        return super.getId();
    }
    @Id
    public void setId(String id){
        super.setId(id);
    }
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
