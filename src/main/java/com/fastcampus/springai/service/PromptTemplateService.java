package com.fastcampus.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class PromptTemplateService {
    private final ChatModel chatModel;
    private final ChatClient defaultPromptChatClient;

    public PromptTemplateService(
            @Qualifier("vertexAiGeminiChat") ChatModel chatModel,
            @Qualifier("chatClientWithDefaultPrompt") ChatClient defaultPromptChatClient
    ) {
        this.chatModel = chatModel;
        this.defaultPromptChatClient = defaultPromptChatClient;
    }

    public String makePromptTemplateWithParameter(String param) {
        return ChatClient.create(chatModel).prompt()
                .user(u -> u
                        .text("""
                                {
                                    "name":"Bill",
                                    "age":26,
                                    "gender":"M"
                                }
                                
                                Make this JSON object into <param> object.
                                """)
                        .param("param", param))
                .templateRenderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
                .call()
                .content();
    }

    public String defaultSystemPrompt(String user, String voice) {
        return defaultPromptChatClient.prompt()
                .system(sp -> sp.param("voice", voice))
                .user(user)
                .call()
                .content();
    }
}
