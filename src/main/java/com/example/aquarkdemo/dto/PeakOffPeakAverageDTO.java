package com.example.aquarkdemo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PeakOffPeakAverageDTO implements Serializable {

    @JsonProperty("v1")
    @Schema(name = "avgV1", description = "鋰電池電壓 V (消峰時段平均)", example = "1.0")
    private Double avgV1;

    @JsonProperty("v5")
    @Schema(name = "avgV5", description = "太陽能板1電壓 V (消峰時段平均)", example = "1.0")
    private Double avgV5;

    @JsonProperty("v6")
    @Schema(name = "avgV6", description = "太陽能板2電壓 V (消峰時段平均)", example = "1.0")
    private Double avgV6;

    @JsonProperty("rain_d")
    @Schema(name = "avgRainD", description = "雨量 (消峰時段平均)", example = "1.0")
    private Double avgRainD;

    @JsonProperty("rh")
    @Schema(name = "avgRh", description = "濕度 (%) (消峰時段平均)", example = "1.0")
    private Double avgRh;

    @JsonProperty("tx")
    @Schema(name = "avgTx", description = "溫度 (°C) (消峰時段平均)", example = "1.0")
    private Double avgTx;

    @JsonProperty("echo")
    @Schema(name = "avgEcho", description = "水位空高 m (消峰時段平均)", example = "1.0")
    private Double avgEcho;

    @JsonProperty("speed")
    @Schema(name = "avgSpeed", description = "表面流速 (消峰時段平均)", example = "1.0")
    private Double avgSpeed;

    public PeakOffPeakAverageDTO(Double avgV1, Double avgV5, Double avgV6, Double avgRainD, Double avgRh, Double avgTx, Double avgEcho, Double avgSpeed) {
        this.avgV1 = avgV1;
        this.avgV5 = avgV5;
        this.avgV6 = avgV6;
        this.avgRainD = avgRainD;
        this.avgRh = avgRh;
        this.avgTx = avgTx;
        this.avgEcho = avgEcho;
        this.avgSpeed = avgSpeed;
    }
}