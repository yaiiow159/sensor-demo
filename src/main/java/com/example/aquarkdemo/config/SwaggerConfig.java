package com.example.aquarkdemo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 統一配置 openapi 3
 */
@Configuration
public class SwaggerConfig {

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
