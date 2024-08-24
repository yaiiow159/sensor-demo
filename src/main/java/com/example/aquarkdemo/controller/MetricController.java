package com.example.aquarkdemo.controller;

import com.example.aquarkdemo.service.RedisMetricService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Tag(name = "緩存監控相關API功能", description = "Cache Metric API")
@Slf4j
@Controller
@RequestMapping("/api/v1/metric")
@RequiredArgsConstructor
public class MetricController {

    private final RedisMetricService redisMetricService;

    @GetMapping
    @Operation(summary = "進入緩存監控頁面",description = "進入緩存監控頁面")
    public String intoRedisMetricPage(){
        return "redis-metric";
    }

    @GetMapping("/info")
    @Operation(summary = "獲取緩存監控信息",description = "獲取緩存監控信息")
    @ResponseBody
    public Map<String, Object> queryMetricInfo(){
        return redisMetricService.getMetricData();
    }
}
