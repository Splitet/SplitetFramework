package com.kloia.sample.commands;

import com.kloia.eventapis.api.CommandHandler;
import com.kloia.eventapis.api.EventRepository;
import com.kloia.eventapis.api.RollbackSpec;
import com.kloia.eventapis.api.ViewQuery;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.view.EntityFunctionSpec;
import com.kloia.sample.dto.command.ProcessOrderCommandDto;
import com.kloia.sample.dto.event.ReserveStockEvent;
import com.kloia.sample.model.Orders;
import com.kloia.sample.model.OrderState;
import com.kloia.sample.model.PaymentInformation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
@Slf4j
@RestController
public class ProcessOrderCommand implements CommandHandler<Orders, ProcessOrderCommandDto> {
    private final EventRepository eventRepository;
    private final ViewQuery<Orders> orderQuery;

    @Autowired
    public ProcessOrderCommand(EventRepository eventRepository, ViewQuery<Orders> orderQuery) {
        this.eventRepository = eventRepository;
        this.orderQuery = orderQuery;
    }

    @RequestMapping(value = "/order/v1/{orderId}/process", method = RequestMethod.POST)
    public EventKey execute(@PathVariable("orderId") String orderId, @RequestBody @Valid ProcessOrderCommandDto dto) throws Exception {
        dto.setOrderId(orderId);
        return this.execute(dto);
    }

    @Override
    public EventKey execute(@RequestBody ProcessOrderCommandDto dto) throws Exception {
        Orders order = orderQuery.queryEntity(dto.getOrderId());

        if (order.getState() == OrderState.INITIAL) {
            ReserveStockEvent reserveStockEvent = new ReserveStockEvent(order.getStockId(), order.getOrderAmount(), dto.getPaymentInformation());
            log.info("Template account saved: " + dto);
            return eventRepository.recordAndPublish(order,reserveStockEvent);
        } else
            throw new EventStoreException("Order state is not valid for this Operation: " + dto);
    }

    @Component
    public static class ProcessOrderSpec extends EntityFunctionSpec<Orders, ReserveStockEvent> {
        public ProcessOrderSpec() {
            super((order, event) -> {
                ReserveStockEvent eventData = event.getEventData();
                PaymentInformation paymentInformation = eventData.getPaymentInformation();
                order.setPaymentAddress(paymentInformation.getPaymentAddress());
                order.setAmount(paymentInformation.getAmount());
                order.setCardInformation(paymentInformation.getCardInformation());
                order.setState(OrderState.PROCESSING);
                return order;
            });
        }
    }
    @Component
    public static class ProcessOrderRollback implements RollbackSpec<ReserveStockEvent>{
        @Override
        public void rollback(ReserveStockEvent event) {
            log.warn("ProcessOrderRollback for :"+event);
        }
    }
}
