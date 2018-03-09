package com.kloia.sample.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.api.ViewQuery;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.sample.commands.CreateStockCommandHandler;
import com.kloia.sample.model.Stock;
import com.kloia.sample.repository.StockRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


/**
 * Created by zeldalozdemir on 09/02/2017.
 */
@Slf4j
@RestController
@RequestMapping(value = "/stock/v1/")
@EnableFeignClients
public class StockRestController {

    @Autowired
    CreateStockCommandHandler createStockCommandHandler;
    @Autowired
    private ViewQuery<Stock> stockViewQuery;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @RequestMapping(value = "/{stockId}", method = RequestMethod.GET)
    public ResponseEntity<?> getStock(@PathVariable("stockId") String stockId) throws IOException, EventStoreException {

        Stock one = stockRepository.findOne(stockId);
        Stock responseDto = new Stock();
        BeanUtils.copyProperties(one, responseDto);
        return new ResponseEntity<Object>(responseDto, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{stockId}/{version}", method = RequestMethod.GET)
    public ResponseEntity<?> getStockWithVersion(@PathVariable("stockId") String stockId, @PathVariable("version") Integer version) throws IOException, EventStoreException {

        return new ResponseEntity<Object>(stockViewQuery.queryEntity(stockId, version), HttpStatus.CREATED);
    }

/*   @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity<?> createStock(@RequestBody CreateStockCommandDto createStockCommandDto) throws Exception {
//        TemplateAccount saved = createTemplateAccountService.create(orderCreateDTO);
        log.info("Create Stock CommandHandler: " + createStockCommandDto);

       EventKey execute = createStockCommandHandler.execute(createStockCommandDto);

       return new ResponseEntity<Object>(stockEventRepository.queryEntity(execute.getEntityId()), HttpStatus.CREATED);
    }*/

/*
    @RequestMapping(value = "/process", method = RequestMethod.POST)
    public ResponseEntity<?> processOrder(@RequestBody @Valid ProcessOrderCommandDto processOrderCommandDto) throws Exception {

        itemSoldCommand.execute(processOrderCommandDto);

        return new ResponseEntity<Object>(stockEventRepository.queryEntity(processOrderCommandDto.getOrderId()), HttpStatus.CREATED);


    }*/


}

