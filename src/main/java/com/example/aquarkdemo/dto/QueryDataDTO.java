package com.example.aquarkdemo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


@Data
@NoArgsConstructor
public class QueryDataDTO implements Serializable {

    @Schema(name = "startDate", description = "開始時間", example = "2024-08-11")
    private String startDate;

    @Schema(name = "endDate", description = "結束時間", example = "2024-08-12")
    private String endDate;

    @Schema(name = "timePeriod", description = "查詢類型(尖峰,離峰)", example = "off-Peak, peak")
    private String timePeriod;

    @Schema(name = "selectType", description = "查詢類型(總和,平均)", example = "sum, avg")
    private String selectType;

    @Schema(name = "limit", description = "限制筆數", example = "10")
    private Integer limit;

    @Schema(name = "fields", description = "欄位", example = "[v1,v5,rh]")
    private List<String> field;
}
