package com.example.aquarkdemo.queryType;

public enum QueryTypeEnum {

    DAILY_AVERAGE("dailyAverage"),
    HOURLY_SUM("hourlySum"),
    HOURLY_AVERAGE("hourlyAverage"),
    DAILY_SUM("dailySum"),
    PEAK_TIME_AVERAGE("peakTimeAverage"),
    PEAK_TIME_SUM("peakTimeSum"),
    OFF_PEAK_TIME_AVERAGE("offPeakTimeAverage"),
    OFF_PEAK_TIME_SUM("offPeakTimeSum");

    private final String value;

    QueryTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static QueryTypeEnum fromValue(String value) {
        for (QueryTypeEnum type : QueryTypeEnum.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("無此類型 " + value);
    }
}
