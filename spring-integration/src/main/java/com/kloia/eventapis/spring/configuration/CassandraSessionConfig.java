package com.kloia.eventapis.spring.configuration;

import com.kloia.eventapis.cassandra.CassandraSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

@Configuration
public class CassandraSessionConfig {

    @Autowired
    private CassandraSession cassandraSession;

    @PreDestroy
    public void destroy() {
        cassandraSession.destroy();
    }
}
