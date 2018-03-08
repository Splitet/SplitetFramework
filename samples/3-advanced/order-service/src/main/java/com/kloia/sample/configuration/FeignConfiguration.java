package com.kloia.sample.configuration;

import feign.Feign;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Slf4j
@Configuration
public class FeignConfiguration {

    @Bean
    public Feign.Builder feignBuilder(@Autowired List<RequestInterceptor> interceptors, @Autowired ErrorDecoder errorDecoder) {
        return Feign.builder()
                .requestInterceptors(interceptors).errorDecoder(errorDecoder);
    }
}
