package com.kloia.sample.dto;

import com.kloia.evented.IEventDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zeldalozdemir on 31/01/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderProcessAggDTO implements IEventDto {
    private long orderId;
    private long price;
    private String address;
    private PaymentProcessAggDTO paymentProcessAggDTO;
    private ItemSoldAggDTO itemSoldAggDTO;
}
