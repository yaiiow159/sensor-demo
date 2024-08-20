package com.example.aquarkdemo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.io.Serializable;

/**
 * @author Timmy
 *
 * StickTxRh實體類
 */
@Schema(name = "StickTxRh", description = "StickTxRh實體類")
@Getter
@Embeddable
public class StickTxRh implements Serializable {

    private Double rh;

    private Double tx;

    public StickTxRh() {
        // 設置預設值
        this.rh = 0.0;
        this.tx = 0.0;
    }

    public void setRh(Double rh) {
        this.rh = rh;
    }

    public void setTx(Double tx) {
        this.tx = tx;
    }
}
