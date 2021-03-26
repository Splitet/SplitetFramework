package io.splitet.sample.controller.commands;

import io.splitet.core.api.Command;
import io.splitet.core.api.CommandHandler;
import io.splitet.core.api.EventRepository;
import io.splitet.core.api.ViewQuery;
import io.splitet.core.common.EventKey;
import io.splitet.core.exception.EventStoreException;
import io.splitet.core.view.EntityFunctionSpec;
import io.splitet.sample.dto.command.ReturnPaymentCommandDto;
import io.splitet.sample.dto.event.PaymentReturnedEvent;
import io.splitet.sample.model.Payment;
import io.splitet.sample.model.PaymentState;
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
public class ReturnPaymentCommand implements CommandHandler {

    private final EventRepository eventRepository;
    private final ViewQuery<Payment> paymentViewQuery;

    @Autowired
    public ReturnPaymentCommand(EventRepository eventRepository, ViewQuery<Payment> paymentViewQuery) {
        this.eventRepository = eventRepository;
        this.paymentViewQuery = paymentViewQuery;
    }

    @RequestMapping(value = "/payment/{paymentId}/return", method = RequestMethod.POST)
    @Command
    public EventKey execute(
            @PathVariable("paymentId") String paymentId,
            @RequestBody @Valid ReturnPaymentCommandDto dto
    ) throws Exception {
        dto.setPaymentId(paymentId);
        Payment payment = paymentViewQuery.queryEntity(dto.getPaymentId());

        if (payment.getState() == PaymentState.PAID) {
            PaymentReturnedEvent paymentReturnedEvent = new PaymentReturnedEvent(payment.getOrderId(), payment.getAmount());
            return eventRepository.recordAndPublish(payment, paymentReturnedEvent);
        } else
            throw new EventStoreException("Payment state is not valid for this Operation: " + dto);
    }

    @Component
    public static class PaymentReturnedSpec extends EntityFunctionSpec<Payment, PaymentReturnedEvent> {
        public PaymentReturnedSpec() {
            super((payment, event) -> {
                PaymentReturnedEvent eventData = event.getEventData();
                payment.setAmount(payment.getAmount() - eventData.getAmount());
                payment.setState(PaymentState.RETURN);
                return payment;
            });
        }
    }


}
