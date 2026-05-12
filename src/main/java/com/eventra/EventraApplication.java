package com.eventra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EventraApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventraApplication.class, args);
    }
}