package com.kloia.eventapis.api.store;

import com.kloia.eventapis.api.pojos.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteQueue;
import org.apache.ignite.IgniteSystemProperties;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.configuration.CollectionConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by mali on 20/01/2017.
 */
@Slf4j
@SpringBootApplication
public class Eventapis {


    public static void main(String[] args) {
        System.setProperty(IgniteSystemProperties.IGNITE_UPDATE_NOTIFIER, String.valueOf(false));
        ConfigurableApplicationContext context = SpringApplication.run(Eventapis.class, args);

    }





}
