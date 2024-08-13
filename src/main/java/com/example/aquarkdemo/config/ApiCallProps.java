package com.example.aquarkdemo.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@ConfigurationProperties(prefix = "sensor.api")
@NoArgsConstructor
@Configuration
@Setter
@Getter
public class ApiCallProps {

    private String call240627;
    private String call240706;
    private String call240708;
    private String call240709;
    private String call240710;

    public List<String> getApiCallList() {
        return List.of(call240627, call240706, call240708, call240709, call240710);
    }
}
