package com.kloia.sample;

import com.kloia.eventapis.api.StoreApi;
import com.kloia.evented.EventStoreException;
import com.kloia.evented.IEventRepository;
import com.kloia.sample.commands.CreateStockCommand;
import com.kloia.sample.commands.ItemSoldCommand;
import com.kloia.sample.dto.ItemSoldAggDTO;
import com.kloia.sample.dto.Stock;
import com.kloia.sample.dto.StockCreateAggDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.io.IOException;

/**
 * Created by zeldalozdemir on 09/02/2017.
 */
@Slf4j
@RequestMapping(value = "/aggr/v1/stock/")
@SpringBootApplication
public class TestStockService {

    @Autowired
    @Qualifier("stockEventRepository")
    private IEventRepository<Stock> stockEventRepository;

    @Autowired
    private StoreApi storeApi;

    @Autowired
    CreateStockCommand createStockCommand;

    @Autowired
    ItemSoldCommand itemSoldCommand;

    @PostConstruct
    public void init() {
        stockEventRepository.addAggregateSpecs(createStockCommand);
        stockEventRepository.addAggregateSpecs(itemSoldCommand);
    }

    @RequestMapping(value = "/{stockId}", method = RequestMethod.GET)
    public ResponseEntity<?> getStock(@PathVariable("stockId") Long stockId) throws IOException, EventStoreException {

        return new ResponseEntity<Object>(stockEventRepository.queryEntity(stockId), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity<?> createStock(@RequestBody @Valid StockCreateAggDTO stockCreateAggDTO) throws IOException, EventStoreException, InterruptedException {
//        TemplateAccount saved = createTemplateAccountService.create(orderCreateDTO);

        createStockCommand.processCommand(stockCreateAggDTO);

        log.info("Stock is created: " + stockCreateAggDTO);


        return new ResponseEntity<Object>(stockEventRepository.queryEntity(stockCreateAggDTO.getStockId()), HttpStatus.CREATED);
    }


    @RequestMapping(value = "/item-sold", method = RequestMethod.POST)
    public ResponseEntity<?> handleItemSold(@RequestBody @Valid ItemSoldAggDTO itemSoldAggDTO) throws IOException, EventStoreException, InterruptedException {
//        TemplateAccount saved = createTemplateAccountService.create(orderCreateDTO);

        itemSoldCommand.processCommand(itemSoldAggDTO);

        log.info("Item sold is calculated: " + itemSoldAggDTO);

        return new ResponseEntity<Object>(stockEventRepository.queryEntity(itemSoldAggDTO.getStockId()), HttpStatus.CREATED);
    }
}
