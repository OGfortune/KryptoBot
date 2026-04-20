package com.oghenemalu.kryptobot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KryptoBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(KryptoBotApplication.class, args);
    }
}
