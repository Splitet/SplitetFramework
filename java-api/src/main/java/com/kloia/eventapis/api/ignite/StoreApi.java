package com.kloia.eventapis.api.ignite;


import com.kloia.eventapis.api.impl.KafkaOperationRepository;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.IgniteSpring;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Arrays;

/**
 * Created by zeldalozdemir on 30/01/2017.
 */
@Configuration
@ConditionalOnMissingBean(KafkaOperationRepository.class)
public class StoreApi {
    private Ignite ignite;


    @Value("${operation-repository.ignite.client-address}")
    private String clientAddress;

    @Bean
    @Scope("singleton")
    @Qualifier("operationIgniteClient")
    @ConditionalOnMissingBean(KafkaOperationRepository.class)
    public Ignite createIgnite(ApplicationContext applicationContext) throws IgniteCheckedException {
        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setClientMode(true);
        cfg.setPeerClassLoadingEnabled(false);
        TcpDiscoverySpi discoSpi = new TcpDiscoverySpi();
        TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
        ipFinder.setAddresses(Arrays.asList(clientAddress.split(",")));
        discoSpi.setIpFinder(ipFinder);
        cfg.setDiscoverySpi(discoSpi);
        cfg.setMetricsLogFrequency(0);
        return IgniteSpring.start(cfg,applicationContext);
    }

}
