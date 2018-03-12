package com.kloia.sample.controller.commands;

import com.kloia.eventapis.api.CommandHandler;
import com.kloia.eventapis.api.EventRepository;
import com.kloia.eventapis.api.ViewQuery;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.view.EntityFunctionSpec;
import com.kloia.sample.dto.command.ReturnPaymentCommandDto;
import com.kloia.sample.dto.event.PaymentReturnedEvent;
import com.kloia.sample.model.Payment;
import com.kloia.sample.model.PaymentState;
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
public class ReturnPaymentCommand implements CommandHandler<ReturnPaymentCommandDto> {
    private final EventRepository eventRepository;
    private final ViewQuery<Payment> paymentViewQuery;

    @Autowired
    public ReturnPaymentCommand(EventRepository eventRepository, ViewQuery<Payment> paymentViewQuery) {
        this.eventRepository = eventRepository;
        this.paymentViewQuery = paymentViewQuery;
    }

    @Override
    public EventRepository getDefaultEventRepository() {
        return eventRepository;
    }

    @RequestMapping(value = "/payment/{paymentId}/return", method = RequestMethod.POST)
    public EventKey execute(@PathVariable("paymentId") String paymentId, @RequestBody @Valid ReturnPaymentCommandDto dto) throws Exception {
        dto.setPaymentId(paymentId);
        return this.execute(dto);
    }

    @Override
    public EventKey execute(@RequestBody ReturnPaymentCommandDto dto) throws Exception {
        Payment payment = paymentViewQuery.queryEntity(dto.getPaymentId());

        if (payment.getState() == PaymentState.PAID) {
            PaymentReturnedEvent paymentReturnedEvent = new PaymentReturnedEvent(payment.getOrderId(), payment.getAmount());
            return eventRepository.recordAndPublish(payment, paymentReturnedEvent);
        } else
            throw new EventStoreException("Payment state is not valid for this Operation: " + dto);
    }

    @Component
    public static class ProcessOrderSpec extends EntityFunctionSpec<Payment, PaymentReturnedEvent> {
        public ProcessOrderSpec() {
            super((payment, event) -> {
                PaymentReturnedEvent eventData = event.getEventData();
                payment.setAmount(payment.getAmount() - eventData.getAmount());
                payment.setState(PaymentState.RETURN);
                return payment;
            });
        }
    }


}
