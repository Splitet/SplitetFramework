package com.kloia.eventapis.api.store;

import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.IgniteSystemProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by mali on 20/01/2017.
 */
@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = {"com.kloia.eventapis.api.store","com.kloia.eventbus"})

public class Eventapis {


    public static void main(String[] args) {
        System.setProperty(IgniteSystemProperties.IGNITE_UPDATE_NOTIFIER, String.valueOf(false));
        ConfigurableApplicationContext context = SpringApplication.run(Eventapis.class, args);

    }





}
