package com.fastcampus.springai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultPromptConfig {
    @Bean
    public ChatClient chatClientWithDefaultPrompt(VertexAiGeminiChatModel vertexAiGeminiChatModel) {
        return ChatClient.builder(vertexAiGeminiChatModel)
                .defaultSystem("You are a friendly chat bot that answers question in the voice of a {voice}")
                .build();
    }
}
