package com.example.aquarkdemo.constant;

/**
 * 常用常數
 */
public class CommonConstant {

    // 尖峰、離峰
    public static final String TIME_PERIOD_PEAK = "peak";
    public static final String TIME_PERIOD_OFF_PEAK = "off-peak";

    public static final String SELECT_TYPE_SUM = "sum";
    public static final String SELECT_TYPE_AVG = "avg";

    // 預設警示值
    public static final Double DEFAULT_ALERT_VALUE = 50.0;

    // 批次相關
    public static final int MAX_BATCH_SIZE = 1000000;
    public static final int BATCH_SIZE = 10000;

    // 感應器緩存
    public static final String REDIS_DAILY_SUM_KEY = "daily_sum";
    public static final String REDIS_PEAK_AVERAGE_KEY = "peak_average";
    public static final String REDIS_PEAK_SUM_KEY = "peak_sum";
    public static final String REDIS_OFF_PEAK_AVERAGE_KEY = "off_peak_average";
    public static final String REDIS_OFF_PEAK_SUM_KEY = "off_peak_sum";
    public static final String REDIS_HOURLY_AVERAGE_KEY = "hourly_average";
    public static final String REDIS_HOURLY_SUM_KEY = "hourly_sum";
    public static final String REDIS_DAILY_AVERAGE_KEY = "daily_average";

    public static final String REDIS_QUERY_KEY_PREFIX = "sensor_data_";

    // 感應器欄位
    public static final String SENSOR_FIELD_NAME_V1 = "v1";
    public static final String SENSOR_FIELD_NAME_V5 = "v5";
    public static final String SENSOR_FIELD_NAME_V6 = "v6";
    public static final String SENSOR_FIELD_NAME_RH = "rh";
    public static final String SENSOR_FIELD_NAME_TX = "tx";
    public static final String SENSOR_FIELD_NAME_ECHO = "echo";
    public static final String SENSOR_FIELD_NAME_RAIN_D = "rain_d";
    public static final String SENSOR_FIELD_NAME_SPEED = "speed";
    public static final String SENSOR_ALL_FIELD = "All";

    // 指標 URL
    public static final String REDIS_METRIC_URL = "http://localhost:9121/metrics";
}
