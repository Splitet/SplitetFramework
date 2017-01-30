package com.kloia.eventapis.pojos;

import lombok.Data;
import org.apache.ignite.binary.BinaryObjectException;
import org.apache.ignite.binary.BinaryReader;
import org.apache.ignite.binary.BinaryWriter;
import org.apache.ignite.binary.Binarylizable;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

/**
 * Created by zeldalozdemir on 25/01/2017.
 */
@Data
public class Transaction implements Externalizable, Binarylizable {
    private List<Event> events;

    private TransactionState transactionState;

    public Transaction() {
    }

    public Transaction(List<Event> events, TransactionState transactionState) {
        this.events = events;
        this.transactionState = transactionState;
    }

    public void writeExternal(ObjectOutput out) throws IOException {

        out.writeUTF(transactionState.name());
        out.writeObject(events);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        transactionState = TransactionState.valueOf(TransactionState.class,in.readUTF());
        events = (List<Event>) in.readObject();
    }

    public void writeBinary(BinaryWriter writer) throws BinaryObjectException {
        // todo fill
    }

    public void readBinary(BinaryReader reader) throws BinaryObjectException {
        // todo fill

    }
}
