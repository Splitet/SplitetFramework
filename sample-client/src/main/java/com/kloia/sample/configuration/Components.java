package com.kloia.sample.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.api.StoreApi;
import com.kloia.evented.AggregateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraTemplate;
import sun.jvm.hotspot.runtime.ObjectMonitor;

@Configuration
public class Components {
    @Bean
    public StoreApi getStoreApi() {
        return StoreApi.createStoreApi("127.0.0.1:7500,127.0.0.1:7501,127.0.0.1:7502");
    }
    @Bean
    public AggregateRepository createAggregateRepository(@Autowired CassandraTemplate cassandraTemplate, @Autowired ObjectMapper objectMapper){
        return new AggregateRepository(cassandraTemplate,objectMapper);
    }



/*    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }*/
}
