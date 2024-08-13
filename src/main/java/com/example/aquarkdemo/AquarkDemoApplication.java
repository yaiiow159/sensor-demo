package com.example.aquarkdemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableKafka
@Slf4j
public class AquarkDemoApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AquarkDemoApplication.class, args);

        // 顯示 port號 和 knife4j url
        String port = context.getEnvironment().getProperty("server.port");
        log.info(
                "Application is running! Access URLs:\n\t" +
                        "Local: \t\thttp://localhost:" + port + "\n\t" +
                        "openAPI3 url: \thttp://localhost:" + port + "/swagger-ui.html" + "\n\t" +
                        "knife4j url: \thttp://127.0.0.1:" + port + "/doc.html"
        );
    }

}
