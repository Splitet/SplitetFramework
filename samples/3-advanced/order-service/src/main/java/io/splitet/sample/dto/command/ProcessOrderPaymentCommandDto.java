package io.splitet.sample.dto.command;

import io.splitet.core.api.CommandDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zeldalozdemir on 31/01/2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessOrderPaymentCommandDto implements CommandDto {

    private String orderId;

}
