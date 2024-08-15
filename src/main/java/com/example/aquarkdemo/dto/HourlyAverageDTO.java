package com.example.aquarkdemo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HourlyAverageDTO extends BaseHourDTO implements Serializable {

    @JsonProperty("v1")
    @Schema(name = "avgV1", description = "鋰電池電壓 V (小時平均)", example = "1.0",defaultValue = "0.0")
    private Double avgV1;

    @JsonProperty("v5")
    @Schema(name = "avgV5", description = "太陽能板1電壓 V (小時平均)", example = "1.0",defaultValue = "0.0")
    private Double avgV5;

    @JsonProperty("v6")
    @Schema(name = "avgV6", description = "太陽能板2電壓 V (小時平均)", example = "1.0",defaultValue = "0.0")
    private Double avgV6;

    @Schema(name = "avgRh", description = "濕度 (%) (小時平均)", example = "1.0",defaultValue = "0.0")
    @JsonProperty("rh")
    private Double avgRh;

    @JsonProperty("tx")
    @Schema(name = "avgTx", description = "溫度 (°C) (小時平均)", example = "1.0",defaultValue = "0.0")
    private Double avgTx;

    @JsonProperty("echo")
    @Schema(name = "avgEcho", description = "水位空高 m (小時平均)", example = "1.0",defaultValue = "0.0")
    private Double avgEcho;

    @JsonProperty("rain_d")
    @Schema(name = "avgRain_d", description = "雨量 (小時平均)", example = "1.0",defaultValue = "0.0")
    private Double avgRain_d;

    @JsonProperty("speed")
    @Schema(name = "avgHourSpeed", description = "表面流速 (小時平均)", example = "1.0",defaultValue = "0.0")
    private Double avgHourSpeed;

    public HourlyAverageDTO(Integer hour, Double avgV1, Double avgV5, Double avgV6, Double avgRain_d,  Double avgRh, Double avgTx, Double avgEcho, Double avgHourSpeed) {
        super(hour);
        this.avgV1 = avgV1;
        this.avgV5 = avgV5;
        this.avgV6 = avgV6;
        this.avgRain_d = avgRain_d;
        this.avgRh = avgRh;
        this.avgTx = avgTx;
        this.avgEcho = avgEcho;
        this.avgHourSpeed = avgHourSpeed;
    }

}