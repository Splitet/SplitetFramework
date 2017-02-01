package com.kloia.eventapis.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by zeldalozdemir on 22/01/2017.
 */
@RestController
public class EventController {

    private static final String TEMPLATE = "Hello, %s!";

    @RequestMapping("/home")
    public ResponseEntity<?> greeting(
            @RequestParam(value = "name", defaultValue = "World") String name) {
        return ResponseEntity.ok(String.format(TEMPLATE, name));
    }
}