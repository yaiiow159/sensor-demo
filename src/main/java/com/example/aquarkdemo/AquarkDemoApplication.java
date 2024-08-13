package com.example.aquarkdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableKafka
public class AquarkDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AquarkDemoApplication.class, args);
    }

}
