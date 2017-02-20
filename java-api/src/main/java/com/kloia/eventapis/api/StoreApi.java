package com.kloia.eventapis.api;


import com.kloia.eventapis.api.impl.AggregateBuilder;
import com.kloia.eventapis.api.impl.OperationRepository;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

import java.util.Arrays;

/**
 * Created by zeldalozdemir on 30/01/2017.
 */
public class StoreApi {
    private Ignite ignite;
    private OperationRepository operationRepository;

    private StoreApi(Ignite ignite, OperationRepository operationRepository) {
        this.ignite = ignite;
        this.operationRepository = operationRepository;
    }
    public static StoreApi createStoreApi(String igniteUrl){
        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setClientMode(true);
        cfg.setPeerClassLoadingEnabled(false);
        TcpDiscoverySpi discoSpi = new TcpDiscoverySpi();
        TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
        ipFinder.setAddresses(Arrays.asList(igniteUrl.split(",")));
        discoSpi.setIpFinder(ipFinder);
        cfg.setDiscoverySpi(discoSpi);
        Ignite ignite = Ignition.start(cfg);
        cfg.setMetricsLogFrequency(0);
        return new StoreApi(ignite,new OperationRepository(ignite));
    }

    public OperationRepository getOperationRepository() {
        return operationRepository;
    }

    public AggregateBuilder getAggregateBuilder() {
        return new AggregateBuilder(operationRepository);
    }
}
