package com.kloia.sample.commands;

import com.kloia.eventapis.api.Command;
import com.kloia.eventapis.api.CommandHandler;
import com.kloia.eventapis.api.EventRepository;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.view.EntityFunctionSpec;
import com.kloia.sample.dto.command.AddStockCommandDto;
import com.kloia.sample.dto.command.CreateStockCommandDto;
import com.kloia.sample.dto.event.StockCreatedEvent;
import com.kloia.sample.model.Stock;
import com.kloia.sample.model.StockState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
@Slf4j
@RestController
public class CreateStockCommandHandler implements CommandHandler {

    private final EventRepository eventRepository;
    private final AddStockCommandHandler addStockCommandHandler;

    public CreateStockCommandHandler(EventRepository eventRepository, AddStockCommandHandler addStockCommandHandler) {
        this.eventRepository = eventRepository;
        this.addStockCommandHandler = addStockCommandHandler;
    }

    @RequestMapping(value = "/stock/create", method = RequestMethod.POST)
    @Command
    public EventKey execute(@RequestBody CreateStockCommandDto dto) throws Exception {
        StockCreatedEvent stockCreatedEvent = new StockCreatedEvent();
        BeanUtils.copyProperties(dto, stockCreatedEvent);
        EventKey eventKey = eventRepository.recordAndPublish(stockCreatedEvent);
        try {
            AddStockCommandDto addStockCommandDto = new AddStockCommandDto(dto.getRemainingStock(), eventKey.getEntityId());
            addStockCommandHandler.execute(eventKey.getEntityId(), addStockCommandDto);
        } catch (Exception e) {
            log.warn("Sub Command Failed:" + e.getMessage());
        }
        return eventKey;
    }

    @Component
    public static class CreateStockSpec extends EntityFunctionSpec<Stock, StockCreatedEvent> {
        public CreateStockSpec() {
            super((stock, event) -> {
                StockCreatedEvent eventData = event.getEventData();
                stock.setRemainingStock(0);
                stock.setStockName(eventData.getStockName());
                stock.setState(StockState.INUSE);
                return stock;
            });
        }
    }

}
