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
public class DailySumDTO implements Serializable {

    @JsonProperty("v1")
    @Schema(name = "sumV1", description = "鋰電池電壓 V (日總)", example = "1.0",defaultValue = "0.0")
    private Double sumV1;

    @JsonProperty("v5")
    @Schema(name = "sumV5", description = "太陽能板1電壓 V (日總)", example = "1.0",defaultValue = "0.0")
    private Double sumV5;

    @JsonProperty("v6")
    @Schema(name = "sumV6", description = "太陽能板2電壓 V (日總)", example = "1.0",defaultValue = "0.0")
    private Double sumV6;

    @JsonProperty("rain_d")
    @Schema(name = "sumRainD", description = "雨量 (日總)", example = "1.0",defaultValue = "0.0")
    private Double sumRainD;

    @JsonProperty("rh")
    @Schema(name = "sumRh", description = "濕度 (%) (日總)", example = "1.0",defaultValue = "0.0")
    private Double sumRh;

    @JsonProperty("tx")
    @Schema(name = "sumTx", description = "溫度 (°C) (日總)", example = "1.0",defaultValue = "0.0")
    private Double sumTx;

    @JsonProperty("echo")
    @Schema(name = "sumEcho", description = "水位空高 m (日總)", example = "1.0",defaultValue = "0.0")
    private Double sumEcho;

    @JsonProperty("speed")
    @Schema(name = "sumSpeed", description = "表面流速 (日總)", example = "1.0",defaultValue = "0.0")
    private Double sumSpeed;

    public DailySumDTO(Double sumV1, Double sumV5, Double sumV6, Double sumRainD, Double sumRh, Double sumTx, Double sumEcho, Double sumSpeed) {
        this.sumV1 = sumV1;
        this.sumV5 = sumV5;
        this.sumV6 = sumV6;
        this.sumRainD = sumRainD;
        this.sumRh = sumRh;
        this.sumTx = sumTx;
        this.sumEcho = sumEcho;
        this.sumSpeed = sumSpeed;
    }
}