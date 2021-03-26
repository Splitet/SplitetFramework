package io.splitet.core.api.emon;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by mali on 20/01/2017.
 */
@Slf4j
@SpringBootApplication

public class EmonApplication {


    public static void main(String[] args) {
        SpringApplication.run(EmonApplication.class, args);

    }


}
