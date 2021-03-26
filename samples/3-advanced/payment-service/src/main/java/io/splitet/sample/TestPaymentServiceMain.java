package io.splitet.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by zeldalozdemir on 30/01/2017.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"io.splitet.sample", "io.splitet.core"})
public class TestPaymentServiceMain {

    public static void main(String[] args) {

        System.setProperty("spring.devtools.restart.enabled", "false");

        SpringApplication.run(TestPaymentServiceMain.class, args);


    }

}