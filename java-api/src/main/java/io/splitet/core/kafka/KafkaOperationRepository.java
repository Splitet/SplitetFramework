package io.splitet.core.kafka;

import io.splitet.core.api.IUserContext;
import io.splitet.core.common.OperationContext;
import io.splitet.core.pojos.Event;
import io.splitet.core.pojos.Operation;
import io.splitet.core.pojos.TransactionState;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * Created by zeldalozdemir on 20/04/2017.
 */
@Slf4j
public class KafkaOperationRepository implements IOperationRepository {
    private OperationContext operationContext;
    private IUserContext userContext;
    private KafkaProducer<String, Operation> operationsKafka;
    private KafkaProducer<String, PublishedEventWrapper> eventsKafka;
    private String senderGroupId;

    public KafkaOperationRepository(OperationContext operationContext,
                                    IUserContext userContext, KafkaProducer<String, Operation> operationsKafka,
                                    KafkaProducer<String, PublishedEventWrapper> eventsKafka,
                                    String senderGroupId) {
        this.operationContext = operationContext;
        this.userContext = userContext;
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
    public void failOperation(String eventId, SerializableConsumer<Event> action) {
        Operation operation = new Operation();
        operation.setSender(senderGroupId);
        operation.setAggregateId(eventId);
        operation.setUserContext(userContext.getUserContext());
        operation.setContext(operationContext.getContext());
        operation.setTransactionState(TransactionState.TXN_FAILED);
        operation.setOpDate(System.currentTimeMillis());
        log.debug("Publishing Operation:" + operation.toString());
        operationsKafka.send(new ProducerRecord<>(Operation.OPERATION_EVENTS, operationContext.getContext().getOpId(), operation));
    }

    @Override
    public void successOperation(String eventId, SerializableConsumer<Event> action) {
        Operation operation = new Operation();
        operation.setSender(senderGroupId);
        operation.setAggregateId(eventId);
        operation.setTransactionState(TransactionState.TXN_SUCCEEDED);
        operation.setUserContext(userContext.getUserContext());
        operation.setContext(operationContext.getContext());
        operation.setOpDate(System.currentTimeMillis());
        log.debug("Publishing Operation:" + operation.toString());
        operationsKafka.send(new ProducerRecord<>(Operation.OPERATION_EVENTS, operationContext.getContext().getOpId(), operation));
    }

    @Override
    public void publishEvent(String topic, String event, long opDate) {
        PublishedEventWrapper publishedEventWrapper = new PublishedEventWrapper(operationContext.getContext(), event, opDate);
        publishedEventWrapper.setUserContext(userContext.getUserContext());
        publishedEventWrapper.setSender(senderGroupId);
        log.debug("Publishing Topic:" + topic + " Event:" + publishedEventWrapper.toString());
        eventsKafka.send(new ProducerRecord<>(topic, operationContext.getContext().getOpId(), publishedEventWrapper));
    }

}
