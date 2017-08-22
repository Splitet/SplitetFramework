package com.kloia.eventapis.spring.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloia.eventapis.common.OperationContext;
import feign.Feign;
import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.List;

@Slf4j
@Configuration
public class FeignHelper {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    OperationContext operationContext;

    /*    @Bean
        public ErrorDecoder errorDecoder() {
            return (methodKey, response) -> {
                String errorDesc = null;
                try {
                    errorDesc = IOUtils.toString(response.body().asInputStream(), "utf-8");
                    return objectMapper.readValue(errorDesc, BaseException.class);
                } catch (Exception e) {
                    log.error("Feign error decoder exception : ", e);
                    if (errorDesc != null) {
                        return new BaseException("Unclassified Error", errorDesc);
                    } else {
                        return new BaseException("Unclassified Error", "Unexpected Error");
                    }
                }
            };
        }*/
    @Bean
    @Scope("prototype")
    public RequestInterceptor opIdInterceptor() {
        return (template) -> {
            String key = this.operationContext.getContext();
            if (key != null) {
                template.header("opId", key);
            }

        };
    }

    @Bean
    public Feign.Builder feignBuilder(@Autowired List<RequestInterceptor> interceptors) {
        return Feign.builder()
                .requestInterceptors(interceptors);
    }


}
