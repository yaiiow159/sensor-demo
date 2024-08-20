package com.example.aquarkdemo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Timmy
 *
 * 原始資料
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RawData implements Serializable {

    @Schema(name = "raw", description = "API原始資料")
    @JsonProperty("raw")
    private List<SensorData> raw;
}
