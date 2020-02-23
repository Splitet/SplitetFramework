package com.kloia.sample.client;

import com.kloia.eventapis.common.EventKey;
import com.kloia.sample.dto.command.ReturnPaymentCommandDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@FeignClient(name = "paymentClient", url = "${payment-client-url}")
public interface PaymentClient {

    @PostMapping(value = "/payment/{paymentId}/return", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    EventKey returnPaymentCommand(@PathVariable("paymentId") String paymentId, @RequestBody ReturnPaymentCommandDto returnPaymentCommandDto);

}
