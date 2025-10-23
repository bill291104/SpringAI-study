package com.fastcampus.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class MultipleChatClientsWithSingleModelTypeService {
    private final ChatModel myChatModel;

    public MultipleChatClientsWithSingleModelTypeService(@Qualifier("vertexAiGeminiChat") ChatModel myChatModel) {
        this.myChatModel = myChatModel;
    }

    public List<String> getResponseFromMultipleChatClientsWithSingleModel(String message) {
        ChatClient defaultChatClient = ChatClient.create(myChatModel);

        ChatClient.Builder builder = ChatClient.builder(myChatModel);
        ChatClient customChatClient = builder.defaultSystem("너는 친근한 도우미야. 사용자에게 반말로 친근하게 대답해.").build();

        String defaultContent = defaultChatClient.prompt()
                .user(message)
                .call()
                .content();

        String customContent = customChatClient.prompt()
                .user(message)
                .call()
                .content();

        return List.of(Objects.requireNonNull(defaultContent), Objects.requireNonNull(customContent));
    }
}
