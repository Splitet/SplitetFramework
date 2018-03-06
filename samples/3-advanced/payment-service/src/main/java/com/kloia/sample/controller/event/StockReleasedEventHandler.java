package com.kloia.sample.controller.event;

import com.kloia.eventapis.api.EventHandler;
import com.kloia.eventapis.api.EventRepository;
import com.kloia.eventapis.api.ViewQuery;
import com.kloia.eventapis.cassandra.ConcurrentEventException;
import com.kloia.eventapis.common.EventKey;
import com.kloia.eventapis.exception.EventPulisherException;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.view.EntityFunctionSpec;
import com.kloia.sample.dto.event.PaymentReturnedEvent;
import com.kloia.sample.dto.event.StockReleasedEvent;
import com.kloia.sample.model.Payment;
import com.kloia.sample.model.PaymentState;
import com.kloia.sample.model.QPayment;
import com.kloia.sample.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
@Slf4j
@RestController
public class StockReleasedEventHandler implements EventHandler<StockReleasedEvent> {
    private final EventRepository eventRepository;
    private final ViewQuery<Payment> paymentViewQuery;
    private final PaymentRepository paymentRepository;


    @Autowired
    public StockReleasedEventHandler(EventRepository eventRepository,
                                     ViewQuery<Payment> paymentViewQuery,
                                     PaymentRepository paymentRepository) {
        this.eventRepository = eventRepository;
        this.paymentViewQuery = paymentViewQuery;
        this.paymentRepository = paymentRepository;
    }

    @KafkaListener(topics = "StockReleasedEvent", containerFactory = "eventsKafkaListenerContainerFactory")
    public EventKey execute(StockReleasedEvent dto) throws EventStoreException, EventPulisherException, ConcurrentEventException {
        Payment result = paymentRepository.findOne(QPayment.payment.orderId.eq(dto.getOrderId()));

        { // to return operations

        }
        return eventRepository.recordAndPublish(result,
                new PaymentReturnedEvent(dto.getOrderId(), result.getPaymentAddress(), result.getAmount(), result.getCardInformation())
        );
    }

    @Component
    public static class DoPaymentSpec extends EntityFunctionSpec<Payment, PaymentReturnedEvent> {
        public DoPaymentSpec() {
            super((payment, event) -> {
                PaymentReturnedEvent paymentReturnEvent = event.getEventData();
                payment.setAmount(paymentReturnEvent.getAmount());
                payment.setCardInformation(paymentReturnEvent.getCardInformation());
                payment.setPaymentAddress(paymentReturnEvent.getPaymentAddress());
                payment.setState(PaymentState.RETURN);
                return payment;
            });
        }
    }
}
