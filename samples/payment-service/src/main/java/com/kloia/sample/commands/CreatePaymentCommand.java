package com.kloia.sample.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.api.impl.OperationRepository;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.evented.*;
import com.kloia.sample.dto.Payment;
import com.kloia.sample.dto.PaymentProcessAggDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 23/02/2017.
 */
@Slf4j
@Controller
public class CreatePaymentCommand extends CommandSpec<Payment, PaymentProcessAggDTO> {
    private final static String name = "CREATE_PAYMENT";
    private OperationRepository operationRepository;
    private final static String CREATED = "CREATED";


    @Autowired
    public CreatePaymentCommand(ObjectMapper objectMapper, OperationRepository operationRepository, IEventRepository<Payment> eventRepository) {
        super(name,objectMapper,eventRepository, (payment, event) -> {
            try {
                if(event.getStatus().equals(CREATED)){
                    PaymentProcessAggDTO paymentProcessAggDTO = objectMapper.readerFor(PaymentProcessAggDTO.class).readValue(event.getEventData());
                    payment = new Payment();
                    payment.setPaymentId(paymentProcessAggDTO.getPaymentId());
                    payment.setAmount(paymentProcessAggDTO.getAmount());
                    payment.setCardInformation(paymentProcessAggDTO.getCardInformation());
                    payment.setPaymentAddress(paymentProcessAggDTO.getPaymentAddress());
                    payment.setState("ISSUED");
                    return payment;
                }
                else
                    return null; // no such issue

            } catch (Exception e) {
                log.error("Error while applying Aggregate:" + event + " Exception:" + e.getMessage(), e);
                throw new EventStoreException("Error while applying Aggregate:" + event + " Exception:" + e.getMessage(), e);
            }
        });

        this.operationRepository = operationRepository;
    }

    @Override
    public void processCommand(PaymentProcessAggDTO paymentProcessAggDTO) throws EventStoreException {
        try {
            Map.Entry<UUID, Operation> context = operationRepository.getContext();
            EntityEvent entityEvent = createEvent(new EventKey(paymentProcessAggDTO.getPaymentId(),0),CREATED, paymentProcessAggDTO,context.getKey());
            getEventRepository().recordAggregateEvent(entityEvent);
        } catch (Exception e) {
            throw new EventStoreException("Error while processing Command:" + paymentProcessAggDTO + " Exception: "+e.getMessage(),e);
        }
    }


}
