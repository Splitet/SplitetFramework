package com.kloia.eventapis.pojos;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import org.junit.Test;

import java.util.Map;

/**
 * Created by zeldalozdemir on 26/01/2017.
 */
public class OperationTest {
    @Test
    public void testReadWriteExternal() throws Exception {
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
        IMap<Object, Object> test = hazelcastInstance.getMap("test");
        test.executeOnKey("123", new EntryProcessor() {
            @Override
            public Object process(Map.Entry entry) {
                entry.setValue("Blabla");
                return entry;
            }

            @Override
            public EntryBackupProcessor getBackupProcessor() {
                return null;
            }
        });

        System.out.println(test.get("123"));


    }

}