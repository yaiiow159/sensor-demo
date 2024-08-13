package com.example.aquarkdemo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@Getter
@Setter
public class Sensor implements Serializable {

    public Sensor() {
        // 設置預設值
        this.volt = new Volt();
        this.stickTxRh = new StickTxRh();
        this.ultrasonicLevel = new UltrasonicLevel();
        this.waterSpeedAquark = new WaterSpeedAquark();
    }

    @Embedded
    @JsonProperty("Volt")
    private Volt volt;

    @Embedded
    @JsonProperty("StickTxRh")
    private StickTxRh stickTxRh;

    @Embedded
    @JsonProperty("Ultrasonic_Level")
    private UltrasonicLevel ultrasonicLevel;

    @Embedded
    @JsonProperty("Water_speed_aquark")
    private WaterSpeedAquark waterSpeedAquark;

}
