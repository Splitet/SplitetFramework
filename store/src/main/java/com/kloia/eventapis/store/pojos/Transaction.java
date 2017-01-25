package com.kloia.eventapis.store.pojos;

import lombok.Data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

/**
 * Created by zeldalozdemir on 25/01/2017.
 */
@Data
public class Transaction implements Externalizable {
    private String eventType;
    private String status;

    public Transaction() {
    }

    public Transaction(String eventType, String status) {
        this.eventType = eventType;
        this.status = status;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(eventType);
        out.writeUTF(status);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        eventType = in.readUTF();
        status = in.readUTF();
    }
}
