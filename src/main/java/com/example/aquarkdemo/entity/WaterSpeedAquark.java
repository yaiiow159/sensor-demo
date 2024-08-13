package com.example.aquarkdemo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Embeddable
public class WaterSpeedAquark implements Serializable {

    @JsonProperty("speed")
    private Double speed;

    public WaterSpeedAquark() {
        // 設置預設值
        this.speed = 0.0;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }
}
