package com.kloia.eventapis.spring.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.common.OperationContext;
import feign.Feign;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration
public class FeignHelper {

    @Autowired
    OperationContext operationContext;
    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            String errorDesc = null;
            try {
                errorDesc = IOUtils.toString(response.body().asInputStream(), "utf-8");
                return objectMapper.readValue(errorDesc, Exception.class);
            } catch (Exception e) {
                log.error("Feign error decoder exception : ", e);
                if (errorDesc != null) {
                    return new Exception("Unclassified Error " + errorDesc);
                } else {
                    return new Exception("Unclassified Error Unexpected Error");
                }
            }
        };
    }

    @Bean
    public Feign.Builder feignBuilder(@Autowired List<RequestInterceptor> interceptors, @Autowired ErrorDecoder errorDecoder) {
        return Feign.builder()
                .requestInterceptors(interceptors).errorDecoder(errorDecoder);
    }


}
