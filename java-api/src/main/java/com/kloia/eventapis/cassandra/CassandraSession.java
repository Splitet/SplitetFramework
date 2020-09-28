package com.kloia.eventapis.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SocketOptions;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.policies.ReconnectionPolicy;
import com.datastax.driver.core.policies.RetryPolicy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import pl.touk.throwing.ThrowingFunction;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by zeldal on 22/05/2017.
 */
@Slf4j
public class CassandraSession {

    private EventStoreConfig eventStoreConfig;
    private Session session;


    public CassandraSession(EventStoreConfig eventStoreConfig) {
        this.eventStoreConfig = eventStoreConfig;
    }

    static <T> T instantiate(Class<T> type) {
        try {
            return type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Cluster cluster() {
        EventStoreConfig properties = eventStoreConfig;
        Cluster.Builder builder = Cluster.builder()
                .withClusterName(properties.getClusterName())
                .withoutJMXReporting()
                .withPort(properties.getPort());
        if (properties.getUsername() != null) {
            builder.withCredentials(properties.getUsername(), properties.getPassword());
        }
        if (properties.getCompression() != null) {
            builder.withCompression(properties.getCompression());
        }
        if (properties.getLoadBalancingPolicy() != null) {
            LoadBalancingPolicy policy = instantiate(properties.getLoadBalancingPolicy());

            builder.withLoadBalancingPolicy(policy);
        }
        builder.withQueryOptions(getQueryOptions());
        if (properties.getReconnectionPolicy() != null) {
            ReconnectionPolicy policy = instantiate(properties.getReconnectionPolicy());
            builder.withReconnectionPolicy(policy);
        }
        if (properties.getRetryPolicy() != null) {
            RetryPolicy policy = instantiate(properties.getRetryPolicy());
            builder.withRetryPolicy(policy);
        }
        builder.withSocketOptions(getSocketOptions());
        if (properties.isSsl()) {
            builder.withSSL();
        }
        String points = properties.getContactPoints();
        builder.addContactPointsWithPorts(Arrays.stream(StringUtils.split(points, ",")).map(s -> {
            String[] split = s.split(":");
            String host = split[0];
            int port = properties.getPort();
            try {
                port = Integer.parseInt(split[1]);
            } catch (Exception e) {
                log.trace(e.getMessage());
            }
            return new InetSocketAddress(host, port);
        }).collect(Collectors.toList()));

        return builder.build();
    }

    private QueryOptions getQueryOptions() {
        QueryOptions options = new QueryOptions();
        if (eventStoreConfig.getConsistencyLevel() != null) {
            options.setConsistencyLevel(eventStoreConfig.getConsistencyLevel());
        }
        if (eventStoreConfig.getSerialConsistencyLevel() != null) {
            options.setSerialConsistencyLevel(eventStoreConfig.getSerialConsistencyLevel());
        }
        options.setFetchSize(eventStoreConfig.getFetchSize());
        return options;
    }

    private SocketOptions getSocketOptions() {
        SocketOptions options = new SocketOptions();
        options.setConnectTimeoutMillis(this.eventStoreConfig.getConnectTimeoutMillis());
        options.setReadTimeoutMillis(this.eventStoreConfig.getReadTimeoutMillis());
        return options;
    }

    private synchronized Session getSession() {
        if (session == null) {
            session = cluster().connect(eventStoreConfig.getKeyspaceName());
        }
        return session;
    }

    public <R, E extends Exception> R execute(Statement st, ThrowingFunction<ResultSet, R, E> tf) throws E {
        return execute(st, Optional.ofNullable(tf));
    }

    public <R, E extends Exception> R execute(Statement st, Optional<ThrowingFunction<ResultSet, R, E>> function) throws E {
        log.trace("Session:" + getSession());
        ResultSet execute = getSession().execute(st);
        return function.isPresent() ? function.get().apply(execute) : (R) execute;
    }

    public ResultSet execute(Statement st) {
        return execute(st, Optional.empty());
    }

    public PreparedStatement prepare(String statement) {
        return getSession().prepare(statement);
    }

    public void destroy() {
        getSession().close();
        getSession().getCluster().close();
    }
}
