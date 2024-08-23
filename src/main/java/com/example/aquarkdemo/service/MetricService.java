package com.example.aquarkdemo.service;

/**
 * 通用取得 Monitoring 指標抽象介面
 */
public interface MetricService {

    /**
     * 取得指標
     * @param metricName 指標名稱
     * @return 指標值
     */
    String getMetricValue(String metricName);

}
