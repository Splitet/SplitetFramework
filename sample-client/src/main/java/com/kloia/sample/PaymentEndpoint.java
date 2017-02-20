package com.kloia.sample;

import com.kloia.sample.configuration.FeignConfiguration;
import com.kloia.sample.dto.OrderCreateAggDTO;
import feign.RequestLine;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by zeldalozdemir on 20/02/2017.
 */
@FeignClient(value = "payment",url = "${payment.url}", configuration = FeignConfiguration.class)
interface PaymentEndpoint{
    @RequestMapping(value = "/aggr/v1/payment/process", method = RequestMethod.POST)
    OrderCreateAggDTO process(@RequestBody OrderCreateAggDTO orderCreateAggDTO);
}
