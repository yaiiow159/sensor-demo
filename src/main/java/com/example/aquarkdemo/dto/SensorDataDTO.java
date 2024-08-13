package com.example.aquarkdemo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SensorDataDTO implements Serializable {

    @Schema(name = "hour", description = "時刻", example = "1")
    private Integer hour;

    @Schema(name = "v1", description = "鋰電池電壓 V", example = "1.0")
    private Double v1;

    @Schema(name = "v5", description = "太陽能板1電壓 V", example = "1.0")
    private Double v5;

    @Schema(name = "v6", description = "太陽能板2電壓 V", example = "1.0")
    private Double v6;

    @Schema(name = "rainD", description = "雨量", example = "1.0")
    private Double rainD;

    @Schema(name = "rh", description = "濕度", example = "1.0")
    private Double rh;

    @Schema(name = "tx", description = "溫度", example = "1.0")
    private Double tx;

    @Schema(name = "echo", description = "水位空高 m", example = "1.0")
    private Double echo;

    @Schema(name = "speed", description = "表面流速", example = "1.0")
    private Double speed;

    @Schema(name = "obsTime", description = "觀測時間", example = "2022-01-01T00:00:00")
    private LocalDateTime obsTime;


    public SensorDataDTO(LocalDateTime obsTime, Double v1, Double v5, Double v6, Double rainD, Double rh, Double tx, Double echo, Double speed) {
        this.v1 = v1;
        this.v5 = v5;
        this.v6 = v6;
        this.rainD = rainD;
        this.rh = rh;
        this.tx = tx;
        this.echo = echo;
        this.speed = speed;
        this.obsTime = obsTime;
    }

}
