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
public class StockCreateAggDTO implements IEventDto {
    private long stockId;
    private String stockName;
    private long remainingStock;
}
