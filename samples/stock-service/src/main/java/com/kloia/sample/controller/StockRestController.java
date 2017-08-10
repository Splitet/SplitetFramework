package com.kloia.sample.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.pojos.EventKey;
import com.kloia.evented.EventStoreException;
import com.kloia.evented.IEventRepository;
import com.kloia.sample.commands.CreateStockCommand;
import com.kloia.sample.dto.command.CreateStockCommandDto;
import com.kloia.sample.model.Stock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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
    private IEventRepository<Stock> stockEventRepository;


    @Autowired
    private ObjectMapper objectMapper;




    @Autowired
    CreateStockCommand createStockCommand;



    @RequestMapping(value = "/{stockId}", method = RequestMethod.GET)
    public ResponseEntity<?> getOrder(@PathVariable("stockId") String stockId) throws IOException, EventStoreException {

        return new ResponseEntity<Object>(stockEventRepository.queryEntity(stockId), HttpStatus.CREATED);
    }

   @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity<?> createStock(@RequestBody CreateStockCommandDto createStockCommandDto) throws Exception {
//        TemplateAccount saved = createTemplateAccountService.create(orderCreateDTO);
        log.info("Create Stock Command: " + createStockCommandDto);

       EventKey execute = createStockCommand.execute(createStockCommandDto);

       return new ResponseEntity<Object>(stockEventRepository.queryEntity(execute.getEntityId()), HttpStatus.CREATED);
    }

/*
    @RequestMapping(value = "/process", method = RequestMethod.POST)
    public ResponseEntity<?> processOrder(@RequestBody @Valid ProcessOrderCommandDto processOrderCommandDto) throws Exception {

        itemSoldCommand.execute(processOrderCommandDto);

        return new ResponseEntity<Object>(stockEventRepository.queryEntity(processOrderCommandDto.getOrderId()), HttpStatus.CREATED);


    }*/


}

