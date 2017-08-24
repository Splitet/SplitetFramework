package com.kloia.eventbus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.UUID;

public class JsonDeserializerWithUUID extends JsonDeserializer<UUID> {


    @Override
    public UUID deserialize(String topic, byte[] data) {
        try {
            return super.deserialize(topic, data);
        } catch (SerializationException e) {
            try {
                return UUID.fromString(new String(data));
            } catch (Exception e1) {
                throw e;
            }
        }
    }
}
