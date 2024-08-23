package com.example.aquarkdemo.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "緩存監控相關API功能", description = "Cache Metric API")
@Slf4j
@Controller
@RequestMapping("/api/v1/metric")
@RequiredArgsConstructor
public class MetricController {

    /**
     * 緩存監控畫面
     * @return 緩存監控畫面
     */
    @GetMapping("/cache")
    public String intoCache() {
        return "cache-metric";
    }
}
