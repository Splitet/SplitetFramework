package io.splitet.core.spring.configuration;

import io.splitet.core.cassandra.CassandraSession;
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
