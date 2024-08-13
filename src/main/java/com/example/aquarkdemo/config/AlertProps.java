package com.example.aquarkdemo.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "sensor.alert")
@NoArgsConstructor
@Configuration
@Setter
@Getter
public class AlertProps {

    private Double alertV1;
    private Double alertV5;
    private Double alertV6;
    private Double alertRh;
    private Double alertTx;
    private Double alertEcho;
    private Double alertRain_d;
    private Double alertSpeed;
}
