package com.kloia.sample;

import com.kloia.eventapis.api.filter.FeignConfiguration;
import com.kloia.sample.dto.ItemSoldAggDTO;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by zeldalozdemir on 20/02/2017.
 */
@FeignClient(value = "stock",url = "${stock.url}", configuration = FeignConfiguration.class)
interface StockEndpoint {
    @RequestMapping(value = "${stock.process.path}", method = RequestMethod.POST)
    ItemSoldAggDTO process(@RequestBody ItemSoldAggDTO itemSoldAggDTO);
}
