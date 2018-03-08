package com.kloia.eventapis.pojos;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 25/01/2017.
 */
@Data
public class Operation implements /*Externalizable,Binarylizable,*/  Serializable {
    private static final long serialVersionUID = -2003849346218727591L;
    public static final String OPERATION_EVENTS = "operation-events";
    private List<Event> events;

    private TransactionState transactionState;
    private String aggregateId;
    private String sender;
    private String parentId;
//    private static transient ObjectMapper objectMapper = new ObjectMapper();


    public Operation() {
    }

    public Operation(String mainAggregateName, List<Event> events, TransactionState transactionState) {
        this.aggregateId = mainAggregateName;
        this.events = events;
        this.transactionState = transactionState;
    }

    public Optional<Event> getEventFor(UUID eventID){
       return getEvents().stream().filter(event -> event.getEventId().equals(eventID)).findFirst();
    }

/*    public void writeExternal(ObjectOutput out) throws IOException {

        out.writeUTF(aggregateId);
        out.writeUTF(transactionState.name());
        out.writeObject(events);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        aggregateId = in.readUTF();
        transactionState = TransactionState.valueOf(TransactionState.class, in.readUTF());
        events = (List<Event>) in.readObject();
    }*/

/*    private String getTransactionStateJson() {
        return transactionState.name();
    }

    private void setTransactionStateJson(String transactionStateJson) {
        this.transactionState = TransactionState.valueOf(transactionStateJson);
    }

    private String getEventsJson() throws JsonProcessingException {
        return objectMapper.writeValueAsString(events);
    }
    private void setEventsJson(String eventsJson) throws IOException {
        this.events = objectMapper.readerFor(List.class).readValue(eventsJson);
    }*/

/*    public void writeBinary(BinaryWriter writer) throws BinaryObjectException {
        BinaryRawWriter binaryRawWriter = writer.rawWriter();
        binaryRawWriter.writeString(aggregateId);
        binaryRawWriter.writeEnum(transactionState);
        binaryRawWriter.writeObjectArray(events.toArray(new Event[events.size()]));
    }

    public void readBinary(BinaryReader reader) throws BinaryObjectException {
        BinaryRawReader binaryRawReader = reader.rawReader();
        aggregateId = binaryRawReader.readString();
        transactionState = binaryRawReader.readEnum();
        Object[] objects = binaryRawReader.readObjectArray();
        events = Stream.of(objects).map(Event.class::cast).collect(Collectors.toList());

    }*/

}
