package com.example.aquarkdemo.service.impl;

import com.example.aquarkdemo.service.MetricService;
import com.example.aquarkdemo.util.RestTemplateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisMetricServiceImpl implements MetricService {

    private final RestTemplateUtil restTemplateUtil;

    /**
     * 取得指標
     *
     * @param metricName 指標名稱
     * @return 指標值
     */
    @Override
    public String getMetricValue(String metricName) {
        return null;
    }
}
