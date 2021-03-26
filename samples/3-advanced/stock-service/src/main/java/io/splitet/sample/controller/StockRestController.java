package io.splitet.sample.controller;

import io.splitet.core.api.ViewQuery;
import io.splitet.core.cassandra.EntityEvent;
import io.splitet.core.exception.EventStoreException;
import io.splitet.sample.model.Stock;
import io.splitet.sample.repository.StockRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by zeldalozdemir on 09/02/2017.
 */
@Slf4j
@RestController
@RequestMapping(value = "/stock/")
@EnableFeignClients
public class StockRestController {

    private final ViewQuery<Stock> stockViewQuery;
    private final StockRepository stockRepository;

    public StockRestController(ViewQuery<Stock> stockViewQuery, StockRepository stockRepository) {
        this.stockViewQuery = stockViewQuery;
        this.stockRepository = stockRepository;
    }

    @RequestMapping(value = "/{stockId}", method = RequestMethod.GET)
    public ResponseEntity<?> getStock(@PathVariable("stockId") String stockId) {
        Stock stock = stockRepository.findById(stockId).get();
        return new ResponseEntity<>(stock, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> getStocks() {
        List<Stock> stocks = stockRepository.findAll();
        return new ResponseEntity<>(stocks, HttpStatus.OK);
    }

    @RequestMapping(value = "/{stockId}/{version}", method = RequestMethod.GET)
    public ResponseEntity<?> getStockWithVersion(
            @PathVariable("stockId") String stockId, @PathVariable("version") Integer version
    ) throws Exception {
        Stock stock = stockViewQuery.queryEntity(stockId, version);
        return new ResponseEntity<>(stock, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{stockId}/history", method = RequestMethod.GET)
    public ResponseEntity<?> getStockHistory(@PathVariable("stockId") String stockId) throws EventStoreException {
        List<EntityEvent> history = stockViewQuery.queryHistory(stockId);
        return new ResponseEntity<>(history, HttpStatus.OK);
    }

}

