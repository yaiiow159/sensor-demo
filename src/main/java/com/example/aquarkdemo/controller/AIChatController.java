package com.example.aquarkdemo.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "AI聊天相關API功能", description = "AI API")
@Controller
@RequestMapping("api/v1/ai-chat")
@RequiredArgsConstructor
public class AIChatController {

    private final ChatClient chatClient;
    private final EmbeddingClient embeddingClient;



}
