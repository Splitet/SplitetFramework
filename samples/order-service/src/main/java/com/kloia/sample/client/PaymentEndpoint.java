package com.kloia.sample.client;

import com.kloia.eventapis.configuration.FeignConfiguration;
import com.kloia.sample.dto.command.CreateOrderCommandDto;
import com.kloia.sample.model.PaymentInformation;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by zeldalozdemir on 20/02/2017.
 */
@FeignClient(value = "payment",url = "${payment.url}", configuration = FeignConfiguration.class)
public interface PaymentEndpoint{
//    @RequestMapping(value = "/aggr/v1/payment/process", method = RequestMethod.POST)
    @RequestMapping(value = "${payment.process.path}", method = RequestMethod.POST)
    CreateOrderCommandDto process(@RequestBody PaymentInformation paymentInformation);
}
