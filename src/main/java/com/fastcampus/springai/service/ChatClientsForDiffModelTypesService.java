package com.fastcampus.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ChatClientsForDiffModelTypesService {
    private final ChatClient vertexAiChatClient;
    private final ChatClient openAiChatClient;

    public ChatClientsForDiffModelTypesService(
            @Qualifier("vertexAiChatClient") ChatClient vertexAiChatClient,
            @Qualifier("openAiChatClient") ChatClient openAiChatClient
    ) {
        this.vertexAiChatClient = vertexAiChatClient;
        this.openAiChatClient = openAiChatClient;
    }

    public List<String> getIntroductionOfDiffModelTypes() {
        String vertexAiContent = vertexAiChatClient.prompt().user("너가 어떤 모델인지 소개해줘.").call().content();
        String openAiContent = openAiChatClient.prompt().user("너가 어떤 모델인지 소개해줘.").call().content();

        return List.of(Objects.requireNonNull(vertexAiContent), Objects.requireNonNull(openAiContent));
    }
}
