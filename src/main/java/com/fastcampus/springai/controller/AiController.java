package com.fastcampus.springai.controller;

import com.fastcampus.springai.service.ChatClientsForDiffModelTypesService;
import com.fastcampus.springai.service.MultipleChatClientsWithSingleModelTypeService;
import com.fastcampus.springai.service.MultipleOpenAiCompatibleApiService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AiController {
    private final ChatClient chatClient;

    private final MultipleChatClientsWithSingleModelTypeService multipleChatClientsWithSingleModelTypeService;
    private final ChatClientsForDiffModelTypesService chatClientsForDiffModelTypesService;
    private final MultipleOpenAiCompatibleApiService multipleOpenAiCompatibleApiService;

//    public MyController(ChatClient.Builder chatClientBuilder) {
//        this.chatClient = chatClientBuilder.build();
//    }
    public AiController(
            @Qualifier("vertexAiGeminiChat") ChatModel chatModel,
            MultipleChatClientsWithSingleModelTypeService multipleChatClientsWithSingleModelTypeService,
            ChatClientsForDiffModelTypesService chatClientsForDiffModelTypesService,
            MultipleOpenAiCompatibleApiService multipleOpenAiCompatibleApiService
    ) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.multipleChatClientsWithSingleModelTypeService = multipleChatClientsWithSingleModelTypeService;
        this.chatClientsForDiffModelTypesService = chatClientsForDiffModelTypesService;
        this.multipleOpenAiCompatibleApiService = multipleOpenAiCompatibleApiService;
    }

    @GetMapping("/ai")
    public String generation(String userInput){
        return this.chatClient.prompt()
                .user(userInput)
                .call()
                .content();
    }

    @GetMapping("/ai/multiple-chat-client-with-single-model-type")
    public List<String> multipleChatClientsWithSingleModelType(String message){
        return multipleChatClientsWithSingleModelTypeService.getResponseFromMultipleChatClientsWithSingleModel(message);
    }

    @GetMapping("/ai/chat-clients-for-diff-model-types")
    public List<String> chatClientsForDiffModelTypes(){
        return chatClientsForDiffModelTypesService.getIntroductionOfDiffModelTypes();
    }

    @GetMapping("/ai/multiple-open-ai-compatible-api")
    public List<String> multipleOpenAiCompatibleApi(){
        return multipleOpenAiCompatibleApiService.multiClientFlow();
    }
}
