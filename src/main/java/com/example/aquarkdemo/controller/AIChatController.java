package com.example.aquarkdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "AI相關API功能", description = "AI API")
@Controller
@RequestMapping("api/v1/ai")
@RequiredArgsConstructor
public class AIChatController {

    private final OllamaChatModel ollamaChatModel;

    @GetMapping("/")
    @Operation(summary = "進入AI小助手頁面", description = "進入AI小助手頁面", tags = "查詢")
    public String aichat() {
        return "ai-chat";
    }

    @PostMapping(value = "/chat",produces = "application/json;charset=UTF-8")
    @Operation(summary = "AI聊天", description = "根據前端請求訊息響應AI的訊息", tags = "AI-Chat")
    @ResponseBody
    public String chat(@Parameter(description = "訊息", example = "你好", required = true) @RequestBody String message) {
        ChatResponse chatResponse;
        try {
            chatResponse = ollamaChatModel.call(new Prompt(message));
            log.debug("AI 回應訊息: {}", chatResponse.getResult().getOutput().getContent());
            return chatResponse.getResult().getOutput().getContent();
        } catch (Exception e) {
            if(e.getMessage().contains("429")) return "已超過預定配額，請升級當前配額在進行操作";
            if(e.getMessage().contains("400")) return "請輸入正確的訊息";
            if (e.getMessage().contains("401")) return "未授權";
            if (e.getMessage().contains("500")) return "發生異常、請聯繫管理員";
            log.error("發生異常 原因: {}", e.getMessage());
        }
        return "暫時無法回應您的問題";
    }

}
