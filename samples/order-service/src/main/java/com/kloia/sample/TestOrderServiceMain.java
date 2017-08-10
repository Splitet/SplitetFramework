package com.kloia.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by zeldalozdemir on 30/01/2017.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.kloia.sample","com.kloia.eventapis"})
@EnableFeignClients
public class TestOrderServiceMain {

    public static void main(String[] args) {
        System.setProperty("spring.devtools.restart.enabled", "false");

        SpringApplication.run(TestOrderServiceMain.class, args);

    }

}