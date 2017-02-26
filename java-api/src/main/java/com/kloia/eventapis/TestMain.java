package com.kloia.eventapis;

import com.kloia.eventapis.pojos.Operation;

import java.io.ObjectStreamClass;

/**
 * Created by zeldalozdemir on 30/01/2017.
 */
public class TestMain {
    public static void main(String[] args) {
        ObjectStreamClass objectStream = ObjectStreamClass.lookup(Operation.class);
        System.out.println(objectStream.getSerialVersionUID());
        
    }
}
