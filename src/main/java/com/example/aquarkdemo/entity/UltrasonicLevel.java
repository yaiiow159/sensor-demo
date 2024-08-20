package com.example.aquarkdemo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.io.Serializable;

/**
 * @author Timmy
 *
 * UltrasonicLevel實體類
 */
@Schema(name = "UltrasonicLevel", description = "UltrasonicLevel實體類")
@Getter
@Embeddable
public class UltrasonicLevel implements Serializable {

    private Double echo;

    public UltrasonicLevel() {
        // 設置預設值
        this.echo = 0.0;
    }

    public void setEcho(Double echo) {
        this.echo = echo;
    }
}
