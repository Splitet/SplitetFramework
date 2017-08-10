/*
 * Copyright 2012-2016 the original author or authors.
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

package com.kloia.eventapis.api.impl;

import lombok.Data;
import org.apache.commons.collections4.MapUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
public class KafkaProperties {

    /**
     * Comma-delimited list of host:port pairs to use for establishing the initial
     * connection to the Kafka cluster.
     */
    private List<String> bootstrapServers = new ArrayList<String>(
            Collections.singletonList("localhost:9092"));

    /**
     * Id to pass to the server when making requests; used for server-side logging.
     */
    private String clientId;

    /**
     * Additional properties used to configure the client.
     */
    private Map<String, String> properties = new HashMap<String, String>();

    private final Consumer consumer = new Consumer();

    private final Producer producer = new Producer();

    private Map<String, Object> buildCommonProperties() {
        Map<String, Object> properties = new HashMap<String, Object>();
        if (this.bootstrapServers != null) {
            properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG,
                    this.bootstrapServers);
        }
        if (this.clientId != null) {
            properties.put(CommonClientConfigs.CLIENT_ID_CONFIG, this.clientId);
        }
        if (!MapUtils.isEmpty(this.properties)) {
            properties.putAll(this.properties);
        }
        return properties;
    }

    /**
     * Create an initial map of consumer properties from the state of this instance.
     * <p>
     * This allows you to add additional properties, if necessary, and override the
     * default kafkaConsumerFactory bean.
     *
     * @return the consumer properties initialized with the customizations defined on this
     * instance
     */
    public Map<String, Object> buildConsumerProperties() {
        Map<String, Object> properties = buildCommonProperties();
        properties.putAll(this.consumer.buildProperties());
        return properties;
    }

    /**
     * Create an initial map of producer properties from the state of this instance.
     * <p>
     * This allows you to add additional properties, if necessary, and override the
     * default kafkaProducerFactory bean.
     *
     * @return the producer properties initialized with the customizations defined on this
     * instance
     */
    public Map<String, Object> buildProducerProperties() {
        Map<String, Object> properties = buildCommonProperties();
        properties.putAll(this.producer.buildProperties());
        return properties;
    }


    @Data
    public static class Consumer {

        private Integer autoCommitInterval;

        /**
         * What to do when there is no initial offset in Kafka or if the current offset
         * does not exist any more on the server.
         */
        private String autoOffsetReset;

        /**
         * If true the consumer's offset will be periodically committed in the background.
         */
        private Boolean enableAutoCommit;

        /**
         * Maximum amount of time in milliseconds the server will block before answering
         * the fetch request if there isn't sufficient data to immediately satisfy the
         * requirement given by "fetch.min.bytes".
         */
        private Integer fetchMaxWait;

        /**
         * Minimum amount of data the server should return for a fetch request in bytes.
         */
        private Integer fetchMinSize;

        /**
         * Unique string that identifies the consumer group this consumer belongs to.
         */
        private String groupId;

        /**
         * Expected time in milliseconds between heartbeats to the consumer coordinator.
         */
        private Integer heartbeatInterval;


        /**
         * Maximum number of records returned in a single call to poll().
         */
        private Integer maxPollRecords;


        public Map<String, Object> buildProperties() {
            Map<String, Object> properties = new HashMap<String, Object>();
            if (this.autoCommitInterval != null) {
                properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,
                        this.autoCommitInterval);
            }
            if (this.autoOffsetReset != null) {
                properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                        this.autoOffsetReset);
            }
            if (this.enableAutoCommit != null) {
                properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                        this.enableAutoCommit);
            }
            if (this.fetchMaxWait != null) {
                properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG,
                        this.fetchMaxWait);
            }
            if (this.fetchMinSize != null) {
                properties.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, this.fetchMinSize);
            }
            if (this.groupId != null) {
                properties.put(ConsumerConfig.GROUP_ID_CONFIG, this.groupId);
            }
            if (this.heartbeatInterval != null) {
                properties.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG,
                        this.heartbeatInterval);
            }
            if (this.maxPollRecords != null) {
                properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,
                        this.maxPollRecords);
            }
            return properties;
        }

    }

    @Data
    public static class Producer {
        /**
         * Number of acknowledgments the producer requires the leader to have received
         * before considering a request complete.
         */
        private String acks;

        /**
         * Number of records to batch before sending.
         */
        private Integer batchSize;

        /**
         * Comma-delimited list of host:port pairs to use for establishing the initial
         * connection to the Kafka cluster.
         */
        private List<String> bootstrapServers;

        /**
         * Total bytes of memory the producer can use to buffer records waiting to be sent
         * to the server.
         */
        private Long bufferMemory;

        /**
         * Id to pass to the server when making requests; used for server-side logging.
         */
        private String clientId;

        /**
         * Compression type for all data generated by the producer.
         */
        private String compressionType;

        /**
         * When greater than zero, enables retrying of failed sends.
         */
        private Integer retries;


        public Map<String, Object> buildProperties() {
            Map<String, Object> properties = new HashMap<String, Object>();
            if (this.acks != null) {
                properties.put(ProducerConfig.ACKS_CONFIG, this.acks);
            }
            if (this.batchSize != null) {
                properties.put(ProducerConfig.BATCH_SIZE_CONFIG, this.batchSize);
            }
            if (this.bootstrapServers != null) {
                properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                        this.bootstrapServers);
            }
            if (this.bufferMemory != null) {
                properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, this.bufferMemory);
            }
            if (this.clientId != null) {
                properties.put(ProducerConfig.CLIENT_ID_CONFIG, this.clientId);
            }
            if (this.compressionType != null) {
                properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG,
                        this.compressionType);
            }
            if (this.retries != null) {
                properties.put(ProducerConfig.RETRIES_CONFIG, this.retries);
            }

            return properties;
        }

    }

}
