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

package io.splitet.core.kafka;

import lombok.Data;
import org.apache.commons.collections4.MapUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


@Data
public class KafkaProperties implements Cloneable {

    private final Consumer consumer = new Consumer();
    private final Producer producer = new Producer();

    /**
     * Comma-delimited list of host:port pairs to use for establishing the initial
     * connection to the Kafka cluster.
     */
    private List<String> bootstrapServers = new ArrayList<String>(
            Collections.singletonList("localhost:9092"));

    /**
     * Comma-delimited list of host:port pairs to use for establishing the initial
     * connection to the Kafka cluster.
     */
    private List<String> zookeeperServers = new ArrayList<String>(
            Collections.singletonList("localhost:2181"));
    /**
     * Id to pass to the server when making requests; used for server-side logging.
     */
    private String clientId;
    /**
     * Additional properties used to configure the client.
     */
    private Map<String, String> properties = new HashMap<String, String>();

    private final Ssl ssl = new Ssl();

    public Map<String, Object> buildCommonProperties() {
        Map<String, Object> commonProperties = new HashMap<String, Object>();
        if (this.bootstrapServers != null) {
            commonProperties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG,
                    this.bootstrapServers);
        }
        if (this.clientId != null) {
            commonProperties.put(CommonClientConfigs.CLIENT_ID_CONFIG, this.clientId);
        } else if (this.consumer.groupId != null)
            commonProperties.put(CommonClientConfigs.CLIENT_ID_CONFIG, this.consumer.groupId + "-" + new Random().nextInt(1000));

        buildSslOptions(this.ssl, commonProperties);

        if (!MapUtils.isEmpty(this.properties)) {
            commonProperties.putAll(this.properties);
        }
        return commonProperties;
    }

    static void buildSslOptions(Ssl ssl, Map<String, Object> properties) {

        if (ssl.getKeyPassword() != null) {
            properties.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG,
                    ssl.getKeyPassword());
        }
        if (ssl.getKeystoreLocation() != null) {
            properties.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG,
                    ssl.getKeystoreLocation());
        }
        if (ssl.getKeystorePassword() != null) {
            properties.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG,
                    ssl.getKeystorePassword());
        }
        if (ssl.getTruststoreLocation() != null) {
            properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,
                    ssl.getTruststoreLocation());
        }
        if (ssl.getTruststorePassword() != null) {
            properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,
                    ssl.getTruststorePassword());
        }
    }

    /**
     * Create an initial map of consumer properties from the state of this instance.
     *
     * <p>This allows you to add additional properties, if necessary, and override the
     * default kafkaConsumerFactory bean.
     *
     * @return the consumer properties initialized with the customizations defined on this
     * instance
     */
    public Map<String, Object> buildConsumerProperties() {
        Map<String, Object> consumerProperties = buildCommonProperties();
        consumerProperties.putAll(this.consumer.buildProperties());
        return consumerProperties;
    }

    /**
     * Create an initial map of producer properties from the state of this instance.
     *
     * <p>This allows you to add additional properties, if necessary, and override the
     * default kafkaProducerFactory bean.
     *
     * @return the producer properties initialized with the customizations defined on this
     * instance
     */
    public Map<String, Object> buildProducerProperties() {
        Map<String, Object> producerProperties = buildCommonProperties();
        producerProperties.putAll(this.producer.buildProperties());
        return producerProperties;
    }

    @Override
    public KafkaProperties clone() {
        try {
            return (KafkaProperties) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }


    @Data
    public static class Consumer implements Cloneable {

        private final Ssl ssl = new Ssl();

        /**
         * Frequency in milliseconds that the consumer offsets are auto-committed to Kafka
         * if 'enable.auto.commit' true.
         */

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
         * Concurrency option, default 1
         */
        private Integer eventConcurrency = 1;

        private Integer eventSchedulerPoolSize = 2;

        private Integer eventConsumerSchedulerPoolSize = 4;

        /**
         * Concurrency option, default 1
         */
        private Integer operationConcurrency = 3;

        private Integer operationSchedulerPoolSize = 1;

        private Integer operationConsumerSchedulerPoolSize = 2;

        /**
         * Expected time in milliseconds between heartbeats to the consumer coordinator.
         */
        private Integer heartbeatInterval;


        /**
         * Maximum number of records returned in a single call to poll().
         */
        private Integer maxPollRecords = 1;

        private Integer sessionTimeout;


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

            buildSslOptions(this.ssl, properties);

            if (this.maxPollRecords != null) {
                properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,
                        this.maxPollRecords);
            }
            if (this.sessionTimeout != null) {
                properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,
                        this.sessionTimeout);
            }
            return properties;
        }


        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

    }

    @Data
    public static class Producer implements Cloneable {

        private final Ssl ssl = new Ssl();
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

            buildSslOptions(ssl, properties);

            return properties;
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    @Data
    public static class Ssl {

        /**
         * Password of the private key in the key store file.
         */
        private String keyPassword;

        /**
         * Location of the key store file.
         */
        private String keystoreLocation;

        /**
         * Store password for the key store file.
         */
        private String keystorePassword;

        /**
         * Location of the trust store file.
         */
        private String truststoreLocation;

        /**
         * Store password for the trust store file.
         */
        private String truststorePassword;

        public String getKeyPassword() {
            return this.keyPassword;
        }

        public void setKeyPassword(String keyPassword) {
            this.keyPassword = keyPassword;
        }

        public String getKeystoreLocation() {
            return this.keystoreLocation;
        }

        public void setKeystoreLocation(String keystoreLocation) {
            this.keystoreLocation = keystoreLocation;
        }

        public String getKeystorePassword() {
            return this.keystorePassword;
        }

        public void setKeystorePassword(String keystorePassword) {
            this.keystorePassword = keystorePassword;
        }

        public String getTruststoreLocation() {
            return this.truststoreLocation;
        }

        public void setTruststoreLocation(String truststoreLocation) {
            this.truststoreLocation = truststoreLocation;
        }

        public String getTruststorePassword() {
            return this.truststorePassword;
        }

        public void setTruststorePassword(String truststorePassword) {
            this.truststorePassword = truststorePassword;
        }

    }

}
