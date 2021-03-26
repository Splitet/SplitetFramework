package io.splitet.sample.controller.event;

import io.splitet.core.api.EventHandler;
import io.splitet.core.api.EventRepository;
import io.splitet.core.cassandra.ConcurrentEventException;
import io.splitet.core.common.EventKey;
import io.splitet.core.exception.EventStoreException;
import io.splitet.core.view.EntityFunctionSpec;
import io.splitet.sample.dto.event.PaymentFailedEvent;
import io.splitet.sample.dto.event.PaymentProcessEvent;
import io.splitet.sample.dto.event.PaymentSuccessEvent;
import io.splitet.sample.model.Payment;
import io.splitet.sample.model.PaymentState;
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
public class PaymentProcessEventHandler implements EventHandler<PaymentProcessEvent> {

    private final EventRepository eventRepository;

    @Autowired
    public PaymentProcessEventHandler(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @KafkaListener(topics = "PaymentProcessEvent", containerFactory = "eventsKafkaListenerContainerFactory")
    public EventKey execute(PaymentProcessEvent dto) throws EventStoreException, ConcurrentEventException {
        PaymentSuccessEvent paymentSuccessEvent = new PaymentSuccessEvent();
        BeanUtils.copyProperties(dto.getPaymentInformation(), paymentSuccessEvent);
        paymentSuccessEvent.setOrderId(dto.getSender().getEntityId());
        if (dto.getPaymentInformation().getAmount() > 2000)
            throw new RuntimeException("Bla Bla");
        if (dto.getPaymentInformation().getAmount() > 1000) {
            PaymentFailedEvent paymentFailedEvent = new PaymentFailedEvent();
            BeanUtils.copyProperties(dto.getPaymentInformation(), paymentFailedEvent);
            paymentFailedEvent.setOrderId(dto.getSender().getEntityId());
            return eventRepository.recordAndPublish(paymentFailedEvent);
        }

        return eventRepository.recordAndPublish(paymentSuccessEvent);

    }

    @Component
    public static class PaymentSuccessSpec extends EntityFunctionSpec<Payment, PaymentSuccessEvent> {
        public PaymentSuccessSpec() {
            super((payment, event) -> {
                PaymentSuccessEvent createOrderCommandDto = event.getEventData();
                payment.setOrderId(createOrderCommandDto.getOrderId());
                payment.setAmount(createOrderCommandDto.getAmount());
                payment.setCardInformation(createOrderCommandDto.getCardInformation());
                payment.setPaymentAddress(createOrderCommandDto.getPaymentAddress());
                payment.setState(PaymentState.PAID);
                return payment;
            });
        }
    }
}
