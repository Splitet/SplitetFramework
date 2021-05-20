package io.splitet.core.api.emon.controller;


import com.hazelcast.core.IMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

/**
 * Created by zeldalozdemir on 04/2020.
 */
@Slf4j
@RestController
@RequestMapping(
        value = CommandController.ENDPOINT
)
public class CommandController {

    static final String ENDPOINT = "/commands";

    @Autowired
    private IMap<String, String> commandsMap;


    @GetMapping
    public ResponseEntity<Set<Map.Entry<String, String>>> getTopics() {
        try {
            Set<Map.Entry<String, String>> entries = commandsMap.entrySet();
            return new ResponseEntity<>(entries, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

}