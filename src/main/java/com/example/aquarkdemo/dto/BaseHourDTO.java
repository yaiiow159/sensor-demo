package com.example.aquarkdemo.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = HourlySumDTO.class, name = "hourlySumDTO"),
        @JsonSubTypes.Type(value = HourlyAverageDTO.class, name = "hourlyAverageDTO")
})
public class BaseHourDTO implements Serializable {
    @Schema(name = "hour", description = "小時", example = "1",defaultValue = "1")
    private Integer hour;
}
