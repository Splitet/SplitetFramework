package com.kloia.sample.dto.command;

import com.kloia.eventapis.api.CommandDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zeldalozdemir on 31/01/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderCommandDto implements CommandDto {
    private String stockId;
    private int orderAmount;
    private String description;
}
