package io.splitet.sample.dto.command;

import io.splitet.core.api.CommandDto;
import io.splitet.sample.model.PaymentInformation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zeldalozdemir on 31/01/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessOrderCommandDto implements CommandDto {
    private String orderId;
    private PaymentInformation paymentInformation;

}
