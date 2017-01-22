package com.kloia.eventapis.rest;

import org.springframework.web.bind.annotation.RestController;

/**
 * Created by zeldalozdemir on 22/01/2017.
 */
@RestController
public class EventController {

    private static final String TEMPLATE = "Hello, %s!";

/*    @RequestMapping("/eeve")
    public HttpEntity<Greeting> greeting(
            @RequestParam(value = "name", required = false, defaultValue = "World") String name) {

        Greeting greeting = new Greeting(String.format(TEMPLATE, name));
        greeting.add(linkTo(methodOn(GreetingController.class).greeting(name)).withSelfRel());

        return new ResponseEntity<Greeting>(greeting, HttpStatus.OK);
    }*/
}