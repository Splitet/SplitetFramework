package com.kloia.sample;

import com.kloia.eventapis.api.filter.FeignConfiguration;
import com.kloia.sample.dto.OrderCreateAggDTO;
import com.kloia.sample.dto.PaymentProcessAggDTO;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by zeldalozdemir on 20/02/2017.
 */
@FeignClient(value = "payment",url = "${payment.url}", configuration = FeignConfiguration.class)
interface PaymentEndpoint{
//    @RequestMapping(value = "/aggr/v1/payment/process", method = RequestMethod.POST)
    @RequestMapping(value = "${payment.process.path}", method = RequestMethod.POST)
    OrderCreateAggDTO process(@RequestBody PaymentProcessAggDTO paymentProcessAggDTO);
}
