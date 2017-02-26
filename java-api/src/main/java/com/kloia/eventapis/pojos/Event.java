package com.kloia.eventapis.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ignite.binary.*;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 26/01/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Event implements Externalizable, Binarylizable, Cloneable {
    private UUID eventId;
    private IEventType eventType;
    private EventState eventState;
    private String[] params;


    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(eventId.toString());
        out.writeUTF(eventState.name());
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        eventId = UUID.fromString(in.readUTF());
        eventState = EventState.valueOf(EventState.class, in.readUTF());

    }

    public void writeBinary(BinaryWriter writer) throws BinaryObjectException {
        BinaryRawWriter binaryRawWriter = writer.rawWriter();
        binaryRawWriter.writeUuid(eventId);
        binaryRawWriter.writeObject(eventType);
        binaryRawWriter.writeEnum(eventState);
        binaryRawWriter.writeStringArray(params);
    }

    public void readBinary(BinaryReader reader) throws BinaryObjectException {
        BinaryRawReader binaryRawReader = reader.rawReader();
        eventId = binaryRawReader.readUuid();
        eventType = binaryRawReader.readObject();
        eventState = binaryRawReader.readEnum();
        params = binaryRawReader.readStringArray();
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public Event success() {
        Event clone = (Event) this.clone();
        clone.setEventState(EventState.SUCCEDEED);
        return clone;
    }

    public Event fail() {
        Event clone = (Event) this.clone();
        clone.setEventState(EventState.FAILED);
        return clone;
    }
}
