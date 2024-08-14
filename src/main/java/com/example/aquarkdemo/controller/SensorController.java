package com.example.aquarkdemo.controller;

import com.example.aquarkdemo.dto.QueryDataDTO;
import com.example.aquarkdemo.dto.SensorDataDTO;
import com.example.aquarkdemo.service.SensorDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

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
    @Operation(summary = "進入首頁", description = "返回首頁")
    public String index() {
        return "index";
    }

    @GetMapping("/dashboard")
    @Operation(summary = "進入儀錶板", description = "返回儀錶板")
    public String dashboard() {
        return "dashboard";
    }

    @PostMapping(value = "/query",produces = "application/x-www-form-urlencoded;charset=UTF-8")
    @Operation(summary = "取得數據", description = "按照前端選擇的條件來取相關數據", tags = "Sensor")
    public String getData(@Parameter(description = "日期", example = "2024-08-12", required = true) @RequestParam("startDate") String startDate,
                          @Parameter(description = "日期", example = "2024-08-13", required = true) @RequestParam("endDate") String endDate,
                          @Parameter(description = "時段", example = "peak/offPeak", required = true) @RequestParam("timePeriod") String timePeriod,
                          @Parameter(description = "選擇類型", example = "sum/avg", required = true) @RequestParam("selectType") String selectType,
                          @Parameter(description = "顯示筆數", example = "10", required = true) @RequestParam("limit") Integer limit,
                          @Parameter(description = "欄位", example = "v1,v5", required = true) @RequestParam("field") String field,Model model) {
        List<SensorDataDTO> sensorDataDTOList =
                sensorDataService.queryData(timePeriod, field, startDate, endDate, selectType, limit);

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

}
