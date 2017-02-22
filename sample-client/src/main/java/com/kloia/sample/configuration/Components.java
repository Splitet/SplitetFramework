package com.kloia.sample.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.api.StoreApi;
import com.kloia.eventapis.api.impl.OperationRepository;
import com.kloia.evented.CassandraEventRepository;
import com.kloia.evented.IEventRepository;
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
    public OperationRepository getOperationRepository(StoreApi storeApi) {
        return storeApi.getOperationRepository();
    }

    @Bean
    public IEventRepository createAggregateRepository(@Autowired CassandraTemplate cassandraTemplate, @Autowired ObjectMapper objectMapper){
        return new CassandraEventRepository(cassandraTemplate,objectMapper);
    }

/*    @Bean
    @Scope("prototype")
    public Feign.Builder feignBuilder() {
        return Feign.builder();
    }*/



/*    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }*/
}
