/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.splitet.core.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;

/**
 * Generic {@link Serializer} for sending Java objects to Kafka as JSON.
 *
 * @param <T> class of the entity, representing messages
 * @author Igor Stepanov
 * @author Artem Bilan
 */
public class JsonSerializer<T> implements Serializer<T> {

    protected final ObjectMapper objectMapper;


    public JsonSerializer(@Nonnull ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void configure(Map<String, ?> configs, boolean isKey) {
        // No-op
    }

    public byte[] serialize(String topic, T data) {
        try {
            byte[] result = null;
            if (data != null) {
                result = this.objectMapper.writeValueAsBytes(data);
            }
            return result;
        } catch (IOException exception) {
            throw new SerializationException("Can't serialize data [" + data + "] for topic [" + topic + "]", exception);
        }
    }

    public void close() {
        // No-op
    }

}
