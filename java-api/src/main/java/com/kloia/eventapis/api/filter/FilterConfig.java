package com.kloia.eventapis.api.filter;

import com.kloia.eventapis.api.StoreApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

/**
 * Created by zeldalozdemir on 14/02/2017.
 */
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean shallowEtagHeaderFilter(@Autowired StoreApi storeApi) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new ReqRepOperationFilter(storeApi));
        registration.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
        registration.addUrlPatterns("/aggr/*");
        return registration;
    }

}
