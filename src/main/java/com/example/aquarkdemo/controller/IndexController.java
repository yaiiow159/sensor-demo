package com.example.aquarkdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "首頁相關API功能", description = "Index API")
@Controller
public class IndexController {

    @GetMapping("/")
    @Operation(summary = "進入首頁", description = "返回首頁", tags = "查詢")
    public String index() {
        return "index";
    }
}
