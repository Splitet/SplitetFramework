package com.kloia.eventapis.api.filter;

import com.kloia.eventapis.api.impl.OperationContext;
import feign.Feign;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.UUID;

/**
 * Created by zeldalozdemir on 20/02/2017.
 */
@Configuration
public class FeignConfiguration {
    /*    @Bean
        public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
            return new BasicAuthRequestInterceptor("user", "password");
        }*/
    @Autowired
    OperationContext operationContext;


    @Bean
    @Scope("prototype")
    public Feign.Builder feignBuilder() {
        RequestInterceptor opIdInjector= template -> {
            UUID key = operationContext.getContext();
            if (key != null)
                template.header("opId", key.toString());
        };
        return Feign.builder().requestInterceptor(opIdInjector);
    }


}
