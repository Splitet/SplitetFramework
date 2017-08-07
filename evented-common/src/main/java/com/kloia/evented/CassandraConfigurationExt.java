package com.kloia.evented;

import com.datastax.driver.core.PlainTextAuthProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.data.cassandra.config.java.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.convert.CustomConversions;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by zeldal on 22/05/2017.
 */
@Slf4j
@Configuration
@EnableCassandraRepositories(basePackages = { "com.kloia.evented" })
public class CassandraConfigurationExt extends AbstractCassandraConfiguration {

    @Value("${cassandra.keyspace-name:'test'}")
    private String keyspaceName;
    @Value("${cassandra.contact-points:'localhost:9042}")
    private String contactPoints;

    @Value("${cassandra.username}")
    private String username;

    @Value("${cassandra.password}")
    private String password;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public CustomConversions customConversions() {
        return new CustomConversions(Arrays.asList(new JsonNodeToStringConverter(objectMapper), new StringToJsonNodeConverter(objectMapper)));
    }

    @Override
    protected String getKeyspaceName() {
        return keyspaceName;
    }


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
        if(username != null && !username.trim().equals(""))
            cluster.setAuthProvider(new PlainTextAuthProvider(username,password));

//        cluster.setContactPoints("cassandra1");
        return cluster;
    }

/*    @Bean
    public CassandraMappingContext cassandraMapping() {
        return new BasicCassandraMappingContext();
    }*/

/*    @Bean
    public CassandraConverter converter(@Autowired CassandraMappingContext cassandraMappingContext) {
        return new MappingCassandraConverter(cassandraMappingContext);
    }*/


/*    @Bean
    public CassandraTemplate cassandraTemplate(@Autowired Session session) throws Exception {
        return new CassandraTemplate(session);
    }*/

    private static class StringToJsonNodeConverter implements Converter<String, JsonNode> {
        private ObjectMapper objectMapper;

        public StringToJsonNodeConverter(ObjectMapper objectMapper) {

            this.objectMapper = objectMapper;
        }

        @Override
        public JsonNode convert(String source) {
            try {
                return objectMapper.readTree(source);
            } catch (IOException e) {
                log.error(e.getMessage(),e);
                throw new RuntimeException(e);
            }
        }
    }
    private static class JsonNodeToStringConverter implements Converter<JsonNode,String> {
        private ObjectMapper objectMapper;

        public JsonNodeToStringConverter(ObjectMapper objectMapper) {

            this.objectMapper = objectMapper;
        }

        @Override
        public String convert(JsonNode source) {
            try {
                return objectMapper.writeValueAsString(source);
            } catch (IOException e) {
                log.error(e.getMessage(),e);
                throw new RuntimeException(e);
            }
        }
    }
}
