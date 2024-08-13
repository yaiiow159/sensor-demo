package com.example.aquarkdemo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Embeddable
public class Volt implements Serializable {

    private Double v1;
    private Double v2;
    private Double v3;
    private Double v4;
    private Double v5;
    private Double v6;
    private Double v7;

    public Volt() {
        // 設置預設值
        this.v1 = 0.0;
        this.v2 = 0.0;
        this.v3 = 0.0;
        this.v4 = 0.0;
        this.v5 = 0.0;
        this.v6 = 0.0;
        this.v7 = 0.0;
    }

    public void setV1(Double v1) {
        this.v1 = v1;
    }

    public void setV2(Double v2) {
        this.v2 = v2;
    }

    public void setV3(Double v3) {
        this.v3 = v3;
    }

    public void setV4(Double v4) {
        this.v4 = v4;
    }

    public void setV5(Double v5) {
        this.v5 = v5;
    }

    public void setV6(Double v6) {
        this.v6 = v6;
    }

    public void setV7(Double v7) {
        this.v7 = v7;
    }
}
