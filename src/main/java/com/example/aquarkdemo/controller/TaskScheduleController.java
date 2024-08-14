package com.example.aquarkdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "排程相關API功能", description = "Task Schedule API")
@Slf4j
@Controller
@RequestMapping("api/v1/schedule")
public class TaskScheduleController {

    @GetMapping("/")
    @Operation(summary = "進入排程頁面", description = "進入排程頁面")
    public String taskSchedule() {
        return "task-schedule";
    }
}
