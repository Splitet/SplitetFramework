package io.splitet.sample.commands;

import io.splitet.core.api.Command;
import io.splitet.core.api.CommandHandler;
import io.splitet.core.api.EventRepository;
import io.splitet.core.api.RollbackSpec;
import io.splitet.core.api.ViewQuery;
import io.splitet.core.common.EventKey;
import io.splitet.core.exception.EventStoreException;
import io.splitet.core.view.EntityFunctionSpec;
import io.splitet.sample.dto.command.ProcessOrderCommandDto;
import io.splitet.sample.dto.event.ReserveStockEvent;
import io.splitet.sample.model.OrderState;
import io.splitet.sample.model.Orders;
import io.splitet.sample.model.PaymentInformation;
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
public class ProcessOrderCommand implements CommandHandler {
    private final EventRepository eventRepository;
    private final ViewQuery<Orders> orderQuery;

    @Autowired
    public ProcessOrderCommand(EventRepository eventRepository, ViewQuery<Orders> orderQuery) {
        this.eventRepository = eventRepository;
        this.orderQuery = orderQuery;
    }

    @RequestMapping(value = "/order/{orderId}/process", method = RequestMethod.POST)
    @Command
    public EventKey process(@PathVariable("orderId") String orderId, @RequestBody @Valid ProcessOrderCommandDto dto) throws Exception {
        dto.setOrderId(orderId);
        Orders order = orderQuery.queryEntity(dto.getOrderId());

        if (order.getState() == OrderState.INITIAL) {
            ReserveStockEvent reserveStockEvent = new ReserveStockEvent(order.getStockId(), order.getOrderAmount(), dto.getPaymentInformation());
            log.info("Template account saved: " + dto);
            return eventRepository.recordAndPublish(order, reserveStockEvent);
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
                order.setState(OrderState.RESERVING_STOCK);
                return order;
            });
        }
    }

    @Component
    public static class ProcessOrderRollback implements RollbackSpec<ReserveStockEvent> {
        @Override
        public void rollback(ReserveStockEvent event) {
            log.warn("ProcessOrderRollback for :" + event);
        }
    }
}
