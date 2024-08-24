package com.example.aquarkdemo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 統一配置 openapi 3
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi sensorAPI() {
        return GroupedOpenApi.builder()
                .group("感應器功能 API")
                .pathsToMatch("/api/v1/sensor/**")
                .packagesToScan("com.example.aquarkdemo.controller")
                .build();
    }

    @Bean
    public GroupedOpenApi taskAPI() {
        return GroupedOpenApi.builder()
                .group("排程相關 API")
                .pathsToMatch("/api/v1/schedule/**")
                .packagesToScan("com.example.aquarkdemo.controller")
                .build();
    }

    @Bean
    public GroupedOpenApi aiAPI() {
        return GroupedOpenApi.builder()
                .group("AI相關 API")
                .pathsToMatch("/api/v1/ai/**")
                .packagesToScan("com.example.aquarkdemo.controller")
                .build();
    }

    @Bean
    public GroupedOpenApi indexAPI() {
        return GroupedOpenApi.builder()
                .group("首頁相關 API")
                .pathsToMatch("/")
                .packagesToScan("com.example.aquarkdemo.controller")
                .build();
    }

    @Bean
    public GroupedOpenApi metricsAPI() {
        return GroupedOpenApi.builder()
                .group("緩存監控相關 API")
                .pathsToMatch("/api/v1/metric/**")
                .packagesToScan("com.example.aquarkdemo.controller")
                .build();
    }

    @Bean
    public OpenAPI customizeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sensor Demo").version("1.0")
                        .description("API for Sensor Demo")
                        .license(new License()
                                .name("MIT").url("https://opensource.org/licenses/MIT"))
                        .contact(new io.swagger.v3.oas.models.info
                                .Contact()
                                .name("TimmyChung")
                                .email("examyou076@gmail.com")
                                .url("https://github.com/yaiiow159")))
                .components(new Components());
    }
}
