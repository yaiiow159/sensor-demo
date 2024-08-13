package com.example.aquarkdemo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PeakOffPeakSumDTO implements Serializable {

    @JsonProperty("v1")
    @Schema(name = "sumV1", description = "鋰電池電壓 V (消峰時段總和)", example = "1.0", defaultValue = "0.0")
    private Double sumV1;

    @JsonProperty("v5")
    @Schema(name = "sumV5", description = "太陽能板1電壓 V (消峰時段總和)", example = "1.0", defaultValue = "0.0")
    private Double sumV5;

    @JsonProperty("v6")
    @Schema(name = "sumV6", description = "太陽能板2電壓 V (消峰時段總和)", example = "1.0", defaultValue = "0.0")
    private Double sumV6;

    @JsonProperty("rh")
    @Schema(name = "sumRh", description = "濕度 (%) (消峰時段總和)", example = "1.0", defaultValue = "0.0")
    private Double sumRh;

    @JsonProperty("tx")
    @Schema(name = "sumTx", description = "溫度 (°C) (消峰時段總和)", example = "1.0", defaultValue = "0.0")
    private Double sumTx;

    @JsonProperty("echo")
    @Schema(name = "sumEcho", description = "水位空高 m (消峰時段總和)", example = "1.0", defaultValue = "0.0")
    private Double sumEcho;

    @JsonProperty("rain_d")
    @Schema(name = "sumRain_d", description = "雨量 (消峰時段總和)", example = "1.0", defaultValue = "0.0")
    private Double sumRain_d;

    @Schema(name = "sumHourSpeed", description = "表面流速 (消峰時段總和)", example = "1.0", defaultValue = "0.0")
    @JsonProperty("speed")
    private Double sumHourSpeed;

    public PeakOffPeakSumDTO(Double sumV1, Double sumV5, Double sumV6, Double sumRain_d, Double sumRh, Double sumTx, Double sumEcho, Double sumHourSpeed) {
        this.sumV1 = sumV1;
        this.sumV5 = sumV5;
        this.sumV6 = sumV6;
        this.sumRain_d = sumRain_d;
        this.sumRh = sumRh;
        this.sumTx = sumTx;
        this.sumEcho = sumEcho;
        this.sumHourSpeed = sumHourSpeed;
    }
}
