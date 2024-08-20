package com.example.aquarkdemo.controller;

import com.example.aquarkdemo.service.SensorDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 感應器API控制器
 */
@Tag(name = "感應器相關API功能", description = "Sensor API")
@Controller
@RequestMapping("/api/v1/sensor")
@RequiredArgsConstructor
public class SensorController {

    private final SensorDataService sensorDataService;

    @GetMapping("/")
    @Operation(summary = "進入查詢頁面", description = "進入查詢頁面", tags = "查詢")
    public String query() {
        return "query";
    }

    @GetMapping("/dashboard")
    @Operation(summary = "進入儀錶板", description = "返回儀錶板")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/daily-analysis")
    @Operation(summary = "進入每日分析頁面", description = "進入每日分析頁面", tags = "查詢")
    public String dailyAnalysis() {
        return "daily-analysis";
    }

    @GetMapping("/hourly-analysis")
    @Operation(summary = "進入每小時分析頁面", description = "進入每小時分析頁面", tags = "查詢")
    public String hourlyAnalysis() {
        return "hourly-analysis";
    }

    @GetMapping("/peak-time-analysis")
    @Operation(summary = "進入峰時分析頁面", description = "進入峰時分析頁面", tags = "查詢")
    public String peakTimeAnalysis() {
        return "peak-time-analysis";
    }

    @GetMapping("/off-peak-time-analysis")
    @Operation(summary = "進入尖峰時段分析頁面", description = "進入尖峰時段分析頁面", tags = "查詢")
    public String offPeakTimeAnalysis() {
        return "off-peak-time-analysis";
    }

    @PostMapping(value = "/query",produces = "application/x-www-form-urlencoded;charset=UTF-8")
    @Operation(summary = "取得數據", description = "按照前端選擇的條件來取相關數據", tags = "Sensor")
    public String getData(@Parameter(description = "日期", example = "2024-08-12", required = true) @RequestParam("startDate") String startDate,
                          @Parameter(description = "日期", example = "2024-08-13", required = true) @RequestParam("endDate") String endDate,
                          @Parameter(description = "時段", example = "peak/offPeak", required = true) @RequestParam("timePeriod") String timePeriod,
                          @Parameter(description = "顯示筆數", example = "10", required = true) @RequestParam("limit") Integer limit,
                          @Parameter(description = "欄位", example = "v1,v5", required = true) @RequestParam("field") String field,Model model) {
        List<String> columns = Collections.singletonList(field);
        Object sensorDataDTOList =
                sensorDataService.queryData(timePeriod, field, startDate, endDate, limit);

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("timePeriod", timePeriod);
        model.addAttribute("columns", columns);
        model.addAttribute("sensorDataDTOList", sensorDataDTOList);

        return "result";
    }

    @GetMapping(value = "/dashboard/data",produces = "application/json;charset=UTF-8")
    @Operation(summary = "取得儀錶板數據", description = "取得儀錶板相關數據", tags = "Sensor")
    @ResponseBody
    public Map<String, Object> getDashboardData(@Parameter(description = "日期", example = "2024-08-12", required = true)
                                                @RequestParam("date") String date) {
        return sensorDataService.queryDashboardData(date);
    }

    @GetMapping(value = "/daily-analysis/data",produces = "application/json;charset=UTF-8")
    @Operation(summary = "取得每日分析的相關數據", description = "取得每日分析的相關數據", tags = "Sensor")
    @ResponseBody
    public Map<String, Object> getDailyAnalysis(@Parameter(description = "日期", example = "2024-08-12", required = true)
                                                @RequestParam("date") String date) {
        return sensorDataService.queryDailyAnalysis(date);
    }

    @GetMapping(value = "/hourly-analysis/data",produces = "application/json;charset=UTF-8")
    @Operation(summary = "取得每小時分析的相關數據", description = "取得每小時分析的相關數據", tags = "Sensor")
    @ResponseBody
    public Map<String, Object> getHourlyAnalysis(@Parameter(description = "日期", example = "2024-08-12", required = true)
                                                @RequestParam("date") String date) {
        return sensorDataService.queryHourlyAnalysis(date);
    }

    @GetMapping(value = "/off-peak-analysis/data",produces = "application/json;charset=UTF-8")
    @Operation(summary = "取得離峰時段分析的相關數據", description = "取得離峰時段分析的相關數據", tags = "Sensor")
    @ResponseBody
    public Map<String, Object> getOffPeakAnalysis(@Parameter(description = "日期", example = "2024-08-12", required = true)
                                                @RequestParam("date") String date) {
        return sensorDataService.queryOffPeakAnalysis(date);
    }

    @GetMapping(value = "/peak-analysis/data",produces = "application/json;charset=UTF-8")
    @Operation(summary = "取得尖峰時段分析的相關數據", description = "取得尖峰時段分析的相關數據", tags = "Sensor")
    @ResponseBody
    public Map<String, Object> getPeakAnalysis(@Parameter(description = "日期", example = "2024-08-12", required = true)
                                                @RequestParam("date") String date) {
        return sensorDataService.queryPeakAnalysis(date);
    }



}
