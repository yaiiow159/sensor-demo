package com.example.aquarkdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "AI相關API功能", description = "AI API")
@Controller
@RequestMapping("api/v1/ai")
@RequiredArgsConstructor
public class AIChatController {

    private final ChatClient chatClient;

    @GetMapping("/")
    @Operation(summary = "進入AI小助手頁面", description = "進入AI小助手頁面")
    public String aichat() {
        return "ai-chat";
    }

    @PostMapping(value = "/chat",produces = "application/json;charset=UTF-8")
    @Operation(summary = "AI聊天", description = "根據前端請求訊息響應AI的訊息")
    @ResponseBody
    public String chat(@Parameter(description = "訊息", example = "你好", required = true) @RequestBody String message) {
        String response = "";
        try {
            response = chatClient.call(message);
            log.debug("AI 回應訊息: {}", response);
            return response;
        } catch (Exception e) {
            if(e.getMessage().contains("429")) return "已超過預定配額，請升級當前配額在進行操作";
            if(e.getMessage().contains("400")) return "請輸入正確的訊息";
            if (e.getMessage().contains("401")) return "未授權";
            if (e.getMessage().contains("500")) return "發生異常、請聯繫管理員";
            log.error("發生異常 原因: {}", e.getMessage());
        }
        return response;
    }

}
