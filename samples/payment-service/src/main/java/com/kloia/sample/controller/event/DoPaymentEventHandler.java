package com.kloia.sample.controller.event;

import com.kloia.eventapis.api.EventHandler;
import com.kloia.eventapis.view.EntityFunctionSpec;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.exception.EventPulisherException;
import com.kloia.eventapis.api.EventRepository;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.api.ViewQuery;
import com.kloia.sample.dto.event.PaymentProcessEvent;
import com.kloia.sample.dto.event.PaymentSuccessEvent;
import com.kloia.sample.model.Payment;
import com.kloia.sample.model.PaymentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
@Slf4j
@RestController
public class DoPaymentEventHandler implements EventHandler<PaymentProcessEvent> {
    private final EventRepository eventRepository;
    private final ViewQuery<Payment> paymentQuery;


    @Autowired
    public DoPaymentEventHandler(EventRepository eventRepository, ViewQuery<Payment> paymentQuery) {
        this.eventRepository = eventRepository;
        this.paymentQuery = paymentQuery;
    }

    @KafkaListener(topics = "PaymentProcessEvent", containerFactory = "eventsKafkaListenerContainerFactory")
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
