package com.kloia.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by zeldalozdemir on 30/01/2017.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.kloia.sample","com.kloia.eventapis"})
public class TestStockServiceMain {

    public static void main(String[] args) {

        System.setProperty("spring.devtools.restart.enabled", "false");

        SpringApplication.run(TestStockServiceMain.class, args);


    }

}