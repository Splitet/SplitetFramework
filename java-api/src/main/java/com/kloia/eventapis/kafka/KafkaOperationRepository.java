package com.kloia.eventapis.kafka;

import com.kloia.eventapis.common.Context;
import com.kloia.eventapis.pojos.Event;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.pojos.TransactionState;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.UUID;

/**
 * Created by zeldalozdemir on 20/04/2017.
 */
@Slf4j
public class KafkaOperationRepository implements IOperationRepository {
    private KafkaProducer<String, Operation> operationsKafka;
    private KafkaProducer<String, PublishedEventWrapper> eventsKafka;
    private String senderGroupId;

    public KafkaOperationRepository(KafkaProducer<String, Operation> operationsKafka,
                                    KafkaProducer<String, PublishedEventWrapper> eventsKafka, String senderGroupId) {
        this.operationsKafka = operationsKafka;
        this.eventsKafka = eventsKafka;
        this.senderGroupId = senderGroupId;
    }

    /*    private KafkaTemplate<UUID,Operation> operationsKafka;
    private KafkaTemplate<UUID,PublishedEventWrapper> eventsKafka;

    @Autowired
    public KafkaOperationRepository(@Qualifier("operationsKafka") KafkaTemplate<UUID,Operation> operationsKafka,
                                    @Qualifier("eventsKafka") KafkaTemplate<UUID,PublishedEventWrapper> eventsKafka) {
        this.eventsKafka = eventsKafka;
        this.operationsKafka = operationsKafka;
    }*/

    @Override
    public void failOperation(Context context, String eventId, SerializableConsumer<Event> action) {
        Operation operation = new Operation();
        operation.setSender(senderGroupId);
        operation.setParentId(context.getParentOpId());
        operation.setAggregateId(eventId);
        operation.setTransactionState(TransactionState.TXN_FAILED);
        log.debug("Publishing Operation:" + operation.toString());
        operationsKafka.send(new ProducerRecord<>(Operation.OPERATION_EVENTS, context.getOpId(), operation));
    }

    @Override
    public void successOperation(Context context, String eventId, SerializableConsumer<Event> action) {
        Operation operation = new Operation();
        operation.setSender(senderGroupId);
        operation.setParentId(context.getParentOpId());
        operation.setAggregateId(eventId);
        operation.setTransactionState(TransactionState.TXN_SUCCEDEED);
        log.debug("Publishing Operation:" + operation.toString());
        operationsKafka.send(new ProducerRecord<>(Operation.OPERATION_EVENTS, context.getOpId(), operation));
    }

    public void publishEvent(String name, PublishedEventWrapper event) {
        event.setSender(senderGroupId);
        log.debug("Publishing Topic:" + name + " Event:" + event.toString());
        eventsKafka.send(new ProducerRecord<>(name, event.getOpId(), event));
    }
}
