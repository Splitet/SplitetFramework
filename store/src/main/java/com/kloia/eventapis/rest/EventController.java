package com.kloia.eventapis.rest;

import com.kloia.eventapis.filter.EntityRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.AsyncClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by zeldalozdemir on 22/01/2017.
 */
@RestController
public class EventController {

    private static final String TEMPLATE = "Hello, %s!";

    @Autowired
    private EntityRestTemplate restTemplate;


    @RequestMapping("/home")
    public ResponseEntity<?> greeting(
            @RequestParam(value = "name", defaultValue = "World") String name) {
        return ResponseEntity.ok(String.format(TEMPLATE, name));
    }

    @RequestMapping("/req")
    public ResponseEntity<?> reqInterceptor(){
        String url ="http://google.com";
        HttpMethod method = HttpMethod.GET;

        Map<String,String> urlVariable = new HashMap<String, String>();
        urlVariable.put("q", "Concretepage");

        restTemplate.executeAsync(url, method, urlVariable, header(), null, Void.class);
        return ResponseEntity.ok().build();
    }

    protected HttpHeaders header() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}