package com.kloia.sample.client;

import com.kloia.eventapis.spring.configuration.FeignConfiguration;
import com.kloia.sample.dto.command.ItemSoldDto;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by zeldalozdemir on 20/02/2017.
 */
@FeignClient(value = "stock",url = "${stock.url}", configuration = FeignConfiguration.class)
public interface StockEndpoint {
    @RequestMapping(value = "${stock.process.path}", method = RequestMethod.POST)
    ItemSoldDto process(@RequestBody ItemSoldDto itemSoldAggDTO);
}
