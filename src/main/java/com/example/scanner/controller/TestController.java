package com.example.scanner.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello from Test Controller!";
    }

    @PostMapping("/echo")
    public String echo(@RequestBody String message) {
        return "Echo: " + message;
    }

    @GetMapping("/example")
    public String example() {
        return "This is an example endpoint";
    }
}
