package com.kloia.sample.model;

import com.kloia.evented.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zeldalozdemir on 17/02/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Stock extends Entity {
    private String stockName;
    private long remainingStock;
    private StockState state;
}
