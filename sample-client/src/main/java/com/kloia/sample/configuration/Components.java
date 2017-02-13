package com.kloia.sample.configuration;

import com.kloia.eventapis.StoreApi;
import com.kloia.evented.AggregateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraTemplate;

@Configuration
public class Components {
    @Bean
    public StoreApi getStoreApi() {
        return StoreApi.createStoreApi("127.0.0.1:7500,127.0.0.1:7501,127.0.0.1:7502");
    }
    @Bean
    public AggregateRepository createAggregateRepository(@Autowired CassandraTemplate cassandraTemplate){
        return new AggregateRepository(cassandraTemplate);
    }



/*    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }*/
}
