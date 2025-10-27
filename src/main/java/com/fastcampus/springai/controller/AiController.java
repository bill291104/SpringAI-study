package com.fastcampus.springai.controller;

import com.fastcampus.springai.service.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/ai")
public class AiController {
    private final ChatClient chatClient;

    private final MultipleChatClientsWithSingleModelTypeService multipleChatClientsWithSingleModelTypeService;
    private final ChatClientsForDiffModelTypesService chatClientsForDiffModelTypesService;
    private final MultipleOpenAiCompatibleApiService multipleOpenAiCompatibleApiService;
    private final ChatResponseEntityTypeService chatResponseEntityTypeService;
    private final StreamingResponseService streamingResponseService;
    private final PromptTemplateService promptTemplateService;

//    public MyController(ChatClient.Builder chatClientBuilder) {
//        this.chatClient = chatClientBuilder.build();
//    }
    public AiController(
            @Qualifier("vertexAiGeminiChat") ChatModel chatModel,
            MultipleChatClientsWithSingleModelTypeService multipleChatClientsWithSingleModelTypeService,
            ChatClientsForDiffModelTypesService chatClientsForDiffModelTypesService,
            MultipleOpenAiCompatibleApiService multipleOpenAiCompatibleApiService,
            ChatResponseEntityTypeService chatResponseEntityTypeService,
            StreamingResponseService streamingResponseService,
            PromptTemplateService promptTemplateService
    ) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.multipleChatClientsWithSingleModelTypeService = multipleChatClientsWithSingleModelTypeService;
        this.chatClientsForDiffModelTypesService = chatClientsForDiffModelTypesService;
        this.multipleOpenAiCompatibleApiService = multipleOpenAiCompatibleApiService;
        this.chatResponseEntityTypeService = chatResponseEntityTypeService;
        this.streamingResponseService = streamingResponseService;
        this.promptTemplateService = promptTemplateService;
    }

    @GetMapping("")
    public String generation(String userInput){
        return this.chatClient.prompt()
                .user(userInput)
                .call()
                .content();
    }

    @GetMapping("/multiple-chat-client-with-single-model-type")
    public List<String> multipleChatClientsWithSingleModelType(String message){
        return multipleChatClientsWithSingleModelTypeService.getResponseFromMultipleChatClientsWithSingleModel(message);
    }

    @GetMapping("/chat-clients-for-diff-model-types")
    public List<String> chatClientsForDiffModelTypes(){
        return chatClientsForDiffModelTypesService.getIntroductionOfDiffModelTypes();
    }

    @GetMapping("/multiple-open-ai-compatible-api")
    public List<String> multipleOpenAiCompatibleApi(){
        return multipleOpenAiCompatibleApiService.multiClientFlow();
    }

    @GetMapping("/chat-response-entity-type")
    public ResponseEntity<Void>  chatResponseEntityType(){
        chatResponseEntityTypeService.generateFilmography();
        return ResponseEntity.ok().build();
    }

    @GetMapping(
            value = "/streaming-response",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<String> streamingResponse(){
        return streamingResponseService.streamingResponse();
    }

    @GetMapping("/prompt-template")
    public String promptTemplate(@RequestParam String param){
        return promptTemplateService.makePromptTemplateWithParameter(param);
    }

    @GetMapping("/default-system-prompt")
    public String defaultSystemPrompt(@RequestParam String user, @RequestParam String voice){
        return promptTemplateService.defaultSystemPrompt(user, voice);
    }
}
