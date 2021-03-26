package io.splitet.sample.dto.event;

import io.splitet.core.common.ReceivedEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zeldalozdemir on 31/01/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentSuccessEvent extends ReceivedEvent {
    private String orderId;
}
