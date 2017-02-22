package com.kloia.eventapis.api.pojos;

import lombok.Data;
import org.apache.ignite.binary.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by zeldalozdemir on 25/01/2017.
 */
@Data
public class Operation implements /*Externalizable,*/ Binarylizable {
    private List<Event> events;

    private TransactionState transactionState;
    private String mainAggregateName;

//    private static transient ObjectMapper objectMapper = new ObjectMapper();


    public Operation() {
    }

    public Operation(String mainAggregateName, List<Event> events, TransactionState transactionState) {
        this.mainAggregateName = mainAggregateName;
        this.events = events;
        this.transactionState = transactionState;
    }

    public Optional<Event> getEventFor(UUID eventID){
       return getEvents().stream().filter(event -> event.getEventId().equals(eventID)).findFirst();
    }

/*    public void writeExternal(ObjectOutput out) throws IOException {

        out.writeUTF(mainAggregateName);
        out.writeUTF(transactionState.name());
        out.writeObject(events);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        mainAggregateName = in.readUTF();
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

    public void writeBinary(BinaryWriter writer) throws BinaryObjectException {
        BinaryRawWriter binaryRawWriter = writer.rawWriter();
        binaryRawWriter.writeString(mainAggregateName);
        binaryRawWriter.writeEnum(transactionState);
        binaryRawWriter.writeObjectArray(events.toArray(new Event[events.size()]));
    }

    public void readBinary(BinaryReader reader) throws BinaryObjectException {
        BinaryRawReader binaryRawReader = reader.rawReader();
        mainAggregateName = binaryRawReader.readString();
        transactionState = binaryRawReader.readEnum();
        Object[] objects = binaryRawReader.readObjectArray();
        events = Stream.of(objects).map(Event.class::cast).collect(Collectors.toList());

    }

}
