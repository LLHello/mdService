package com.mdservice.config;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Aiconfig {
    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                // 【核心防御】：只保留最近 10 条消息 (即 5 轮对话：一问一答算两条)
                // 超出的旧消息会被自动丢弃，防止上下文污染
                .maxMessages(6)
                .build();
    }
}
