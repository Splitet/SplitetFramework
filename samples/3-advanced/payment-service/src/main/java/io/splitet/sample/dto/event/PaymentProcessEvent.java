package io.splitet.sample.dto.event;

import io.splitet.core.common.ReceivedEvent;
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
public class PaymentProcessEvent extends ReceivedEvent {
    private PaymentInformation paymentInformation;
}
