package io.code.sutra.activity.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
@Slf4j
public class HelloWorld {
    @RequestMapping("/hello")
    public ResponseEntity<String> hello() {
        log.info("Hello World endpoint invoked");
        return ResponseEntity.ok("Hello, World!");
    }
}
