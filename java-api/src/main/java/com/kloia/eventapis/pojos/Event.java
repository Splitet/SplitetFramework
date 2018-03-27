package com.kloia.eventapis.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class Event implements Externalizable, Cloneable {
    private UUID eventId;
    private IEventType eventType;
    private EventState eventState;
    private String[] params;

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(eventId.toString());
        out.writeUTF(eventState.name());
    }

    public void readExternal(ObjectInput in) throws IOException {
        eventId = UUID.fromString(in.readUTF());
        eventState = EventState.valueOf(EventState.class, in.readUTF());

    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
