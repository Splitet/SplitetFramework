/*
 * Copyright 2015-2016 the original author or authors.
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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * Generic {@link Deserializer} for receiving JSON from Kafka and return Java objects.
 *
 * @param <T> class of the entity, representing messages
 * @author Igor Stepanov
 * @author Artem Bilan
 */
public class JsonDeserializer<T> implements Deserializer<T> {

    protected final ObjectMapper objectMapper;

    protected final Class<T> targetType;

    private volatile ObjectReader reader;


    public JsonDeserializer(Class<T> targetType) {
        this(targetType, new ObjectMapper());
        this.objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @SuppressWarnings("unchecked")
    public JsonDeserializer(Class<T> targetType, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.targetType = targetType;
    }

    public void configure(Map<String, ?> configs, boolean isKey) {
        // No-op
    }

    public T deserialize(String topic, byte[] data) {
        if (this.reader == null) {
            this.reader = this.objectMapper.readerFor(this.targetType);
        }
        try {
            T result = null;
            if (data != null) {
                result = this.reader.readValue(data);
            }
            return result;
        } catch (IOException e) {
            throw new SerializationException("Can't deserialize data [" + Arrays.toString(data) + "] from topic [" + topic + "]", e);
        }
    }

    public void close() {
        // No-op
    }

}
