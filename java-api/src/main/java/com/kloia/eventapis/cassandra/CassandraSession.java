package com.kloia.eventapis.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.kloia.eventapis.cassandra.EventStoreConfig;
import lombok.extern.slf4j.Slf4j;
import pl.touk.throwing.ThrowingFunction;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Optional;

/**
 * Created by zeldal on 22/05/2017.
 */
@Slf4j
public class CassandraSession {

    public CassandraSession(EventStoreConfig eventStoreConfig) {
        this.eventStoreConfig = eventStoreConfig;
    }

    private EventStoreConfig eventStoreConfig;


    private Cluster cluster;

    private Cluster cluster() {
        Cluster.Builder builder = Cluster.builder();
        Arrays.stream(eventStoreConfig.getContactPoints().split(";")).forEach(s -> {
            String[] hostPort = s.split(":");
            builder.addContactPointsWithPorts(new InetSocketAddress(hostPort[0], Integer.parseInt(hostPort[1])));
        });
        builder.withPoolingOptions(eventStoreConfig.getPoolingOptions());

        Cluster cluster = builder.build();
        return cluster.init();
    }

    public Cluster getCluster() {
        if (cluster == null) {
            synchronized (this) {
                if (cluster == null)
                    cluster = cluster();
            }
        }
        return cluster;
    }

    public <R, E extends Exception> R execute(Statement t,ThrowingFunction<ResultSet,R, E> f) throws E {
        return execute(t,Optional.ofNullable(f));
    }
    public <R, E extends Exception> R execute(Statement t, Optional<ThrowingFunction<ResultSet, R, E>> f) throws E {
        try (Session session = getCluster().connect(eventStoreConfig.getKeyspaceName())) {
            ResultSet execute = session.execute(t);
            if(f.isPresent())
                return f.get().apply(execute);
            else
                return (R) execute;
        }
    }
    public ResultSet execute(Statement t) {
       return execute(t,Optional.empty());
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

/*    private static class StringToJsonNodeConverter implements Converter<String, JsonNode> {
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
    }*/
}
