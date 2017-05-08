package com.kloia.evented;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.convert.CassandraConverter;
import org.springframework.data.cassandra.convert.MappingCassandraConverter;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.mapping.BasicCassandraMappingContext;
import org.springframework.data.cassandra.mapping.CassandraMappingContext;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

import java.net.InetSocketAddress;
import java.util.ArrayList;

/**
 * Created by zeldalozdemir on 12/02/2017.
 */
@Slf4j
@Configuration
@EnableCassandraRepositories(basePackages = { "com.kloia.evented" })
public class EventedCassandraConfig {
    @Value("${cassandra.contact-points:'localhost:9042}")
    private String contactPoints;

    @Value("${cassandra.keyspace-name:'test'}")
    private String keyspaceName;

    @Bean
    public CassandraClusterFactoryBean cluster() {

        CassandraClusterFactoryBean cluster = new CassandraClusterFactoryBean();
        ArrayList<InetSocketAddress> addresses = new ArrayList<>();
        try {
            String[] hostPorts = contactPoints.split(";");
            String[] split = hostPorts[0].split(":");
            cluster.setContactPoints(split[0]);
            cluster.setPort(Integer.parseInt(split[1]));
            for (int i = 1; i < hostPorts.length; i++) {
                String hostPort = hostPorts[i];
                split = hostPort.split(":");
                addresses.add(new InetSocketAddress(split[0], Integer.parseInt(split[1])));
            }
        } catch (Exception e) {
            log.error("cassandra.contact-points must be: host1:port1[;host2:port2]... :",e);
            throw e;
        }
        cluster.setClusterBuilderConfigurer(clusterBuilder -> {return clusterBuilder.addContactPointsWithPorts(addresses);});
//        cluster.setContactPoints("cassandra1");
        return cluster;
    }

    @Bean
    public CassandraMappingContext mappingContext() {
        return new BasicCassandraMappingContext();
    }

    @Bean
    public CassandraConverter converter(@Autowired CassandraMappingContext cassandraMappingContext) {
        return new MappingCassandraConverter(cassandraMappingContext);
    }

    @Bean
    public CassandraSessionFactoryBean session(@Autowired Cluster cluster, @Autowired CassandraConverter cassandraConverter)  throws Exception {

        CassandraSessionFactoryBean session = new CassandraSessionFactoryBean();
        session.setCluster(cluster);
        session.setKeyspaceName(keyspaceName);
        session.setConverter(cassandraConverter);
        session.setSchemaAction(SchemaAction.NONE);

        return session;
    }

    @Bean
    public CassandraTemplate cassandraTemplate(@Autowired Session session) throws Exception {
        return new CassandraTemplate(session);
    }
}
