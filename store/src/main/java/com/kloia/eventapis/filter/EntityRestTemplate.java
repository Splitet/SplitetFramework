package com.kloia.eventapis.filter;

import com.kloia.eventapis.pojos.Event;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.pojos.TransactionState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CachePeekMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by mesutcan.gurle on 02/02/17.
 */
@Data
@Slf4j
public class EntityRestTemplate extends RestTemplate {
    @Autowired
    Ignite ignite;

    public Optional<ResponseEntity<?>> executeAsync(String url, HttpMethod method, Map<String, String> urlVariable, HttpHeaders header, Object entity, Class returnType) {
        Optional<ResponseEntity<?>> responseEntity = Optional.empty();
        try {
            responseEntity = Optional.ofNullable(exchange(url, method, new HttpEntity<>(entity, header), returnType, urlVariable));
            IgniteCache<UUID, Operation> transactionCache = ignite.cache("transactionCache");
            log.info("Application is started for KeySizes:"+ transactionCache.size(CachePeekMode.PRIMARY));
            responseEntity.ifPresent( e -> {
                transactionCache.put(UUID.randomUUID(), new Operation(new ArrayList<Event>(), TransactionState.RUNNING));
                log.info("Application is started for KeySizes:" + transactionCache.size(CachePeekMode.PRIMARY));
            });

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return responseEntity;

    }
}
