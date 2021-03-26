package com.kloia.eventapis.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.ProtocolOptions;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.SocketOptions;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.policies.ReconnectionPolicy;
import com.datastax.driver.core.policies.RetryPolicy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventStoreConfig {

    private PoolingOptions poolingOptions = new PoolingOptions(); // default options
    /**
     * Keyspace name to use.
     */
    private String keyspaceName;

    /**
     * Name of the Cassandra cluster.
     */
    private String clusterName;

    /**
     * Comma-separated list of cluster node addresses.
     */
    private String contactPoints = "localhost";

    /**
     * Port of the Cassandra server.
     */
    private int port = ProtocolOptions.DEFAULT_PORT;

    /**
     * Login user of the server.
     */
    private String username;

    /**
     * Login password of the server.
     */
    private String password;

    /**
     * Compression supported by the Cassandra binary protocol.
     */
    private ProtocolOptions.Compression compression = ProtocolOptions.Compression.NONE;

    /**
     * Class name of the load balancing policy.
     */
    private Class<? extends LoadBalancingPolicy> loadBalancingPolicy;

    /**
     * Queries consistency level.
     */
    private ConsistencyLevel consistencyLevel;

    /**
     * Queries serial consistency level.
     */
    private ConsistencyLevel serialConsistencyLevel;

    /**
     * Queries default fetch size.
     */
    private int fetchSize = QueryOptions.DEFAULT_FETCH_SIZE;

    /**
     * Reconnection policy class.
     */
    private Class<? extends ReconnectionPolicy> reconnectionPolicy;

    /**
     * Class name of the retry policy.
     */
    private Class<? extends RetryPolicy> retryPolicy;

    /**
     * Socket option: connection time out.
     */
    private int connectTimeoutMillis = SocketOptions.DEFAULT_CONNECT_TIMEOUT_MILLIS;

    /**
     * Socket option: read time out.
     */
    private int readTimeoutMillis = SocketOptions.DEFAULT_READ_TIMEOUT_MILLIS;

    /**
     * Schema action to take at startup.
     */
    private String schemaAction = "none";

    /**
     * Enable SSL support.
     */
    private boolean ssl;

}
