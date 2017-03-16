package com.kloia.eventapis.api.store;

import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.IgniteSystemProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
<<<<<<< HEAD
<<<<<<< HEAD
import org.springframework.context.annotation.ComponentScan;
=======
>>>>>>> fe1c6bd...  - Evented DB usage examples
=======
import org.springframework.context.annotation.ComponentScan;
>>>>>>> dc25faf...  - Event bus implementation examples

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
