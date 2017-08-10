package com.kloia.sample.commands;

import com.kloia.evented.Command;
import com.kloia.evented.EntityFunctionSpec;
import com.kloia.eventapis.pojos.EventKey;
import com.kloia.evented.EventPulisherException;
import com.kloia.evented.EventRepository;
import com.kloia.evented.EventStoreException;
import com.kloia.evented.Query;
import com.kloia.sample.dto.event.PaymentProcessEvent;
import com.kloia.sample.dto.event.PaymentSuccessEvent;
import com.kloia.sample.model.Payment;
import com.kloia.sample.model.PaymentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
@Slf4j
@Controller
public class DoPaymentCommand implements Command<Payment, PaymentProcessEvent> {
    private final static String name = "CREATE_ORDER";
    private final static String CREATED = "CREATED";
    private final EventRepository<Payment> eventRepository;
    private final Query<Payment> paymentQuery;


    @Autowired
    public DoPaymentCommand(EventRepository<Payment> eventRepository, Query<Payment> paymentQuery) {
        this.eventRepository = eventRepository;
        this.paymentQuery = paymentQuery;
    }

    @Override
    public EventKey execute(PaymentProcessEvent dto) throws EventStoreException, EventPulisherException {



        PaymentSuccessEvent paymentSuccessEvent = new PaymentSuccessEvent();
        BeanUtils.copyProperties(dto.getPaymentInformation(),paymentSuccessEvent);
        paymentSuccessEvent.setOrderId(dto.getSender().getEntityId());

        return eventRepository.recordAndPublish(paymentSuccessEvent);

    }
    @Component
    public static class DoPaymentSpec extends EntityFunctionSpec<Payment, PaymentSuccessEvent> {
        public DoPaymentSpec() {
            super((payment, event) -> {
                PaymentSuccessEvent createOrderCommandDto = event.getEventData();
                payment.setAmount(createOrderCommandDto.getAmount());
                payment.setCardInformation(createOrderCommandDto.getCardInformation());
                payment.setPaymentAddress(createOrderCommandDto.getPaymentAddress());
                payment.setState(PaymentState.PAID);
                return payment;
            });
        }
    }
}
