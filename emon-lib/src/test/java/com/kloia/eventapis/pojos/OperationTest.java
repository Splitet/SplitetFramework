package com.kloia.eventapis.pojos;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import org.junit.Test;

import java.util.Map;
import java.util.regex.Pattern;

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

    @Test
    public void regexpTest() {
        Pattern compile = Pattern.compile("^(.+Event|operation-events)$");
        assert compile.matcher("BalBlaEvent").matches();
        assert !compile.matcher("BalBlaEvent2as").matches();
        assert !compile.matcher("Event").matches();
        assert compile.matcher("operation-events").matches();
        assert !compile.matcher("operation").matches();
        assert !compile.matcher("events").matches();
        assert !compile.matcher("blablaevents").matches();
    }

    @Test
    public void regexpTest2() {
        Pattern compile = Pattern.compile("^(.+command-query|.+-command)$");
        assert compile.matcher("payment3d-process-command").matches();

    }

}