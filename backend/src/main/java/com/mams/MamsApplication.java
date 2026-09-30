package com.mams;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MamsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MamsApplication.class, args);
    }
}
