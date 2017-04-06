package com.kloia.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

/**
 * Created by zeldalozdemir on 30/01/2017.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.kloia.sample", "com.kloia.evented","com.kloia.eventbus", "com.kloia.eventapis.api"})
@EnableCassandraRepositories(basePackages = "com.kloia.evented")
public class TestPaymentServiceMain {

    public static void main(String[] args) {

        System.setProperty("spring.devtools.restart.enabled", "false");

        SpringApplication.run(TestPaymentServiceMain.class, args);


    }

}