package com.fastcampus.springai.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Log4j2
public class MultipleOpenAiCompatibleApiService {
    private final OpenAiChatModel baseChatModel;
    private final OpenAiApi baseOpenAiApi;

    private final String groqApiKey;
    private final String openAiApiKey;

    public MultipleOpenAiCompatibleApiService(
            @Value("${spring.ai.groq.api-key}") String groqApiKey,
            @Value("${spring.ai.openai.api-key}") String openAiApiKey,
            OpenAiChatModel baseChatModel
            ) {
        this.groqApiKey = groqApiKey;
        this.openAiApiKey = openAiApiKey;

        this.baseChatModel = baseChatModel;
        this.baseOpenAiApi = OpenAiApi.builder().apiKey(openAiApiKey).build();
    }

    public List<String> multiClientFlow() {
        try {
            OpenAiApi groqApi = baseOpenAiApi.mutate()
                    .baseUrl("https://api.groq.com/openai")
                    .apiKey(groqApiKey)
                    .build();

            OpenAiApi gpt4Api = baseOpenAiApi.mutate()
                    .baseUrl("https://api.openai.com")
                    .apiKey(openAiApiKey)
                    .build();

            OpenAiChatModel groqModel = baseChatModel.mutate()
                    .openAiApi(groqApi)
                    .defaultOptions(OpenAiChatOptions.builder().model("llama-3.1-8b-instant").temperature(0.5).build())
                    .build();

            OpenAiChatModel gpt4Model = baseChatModel.mutate()
                    .openAiApi(gpt4Api)
                    .defaultOptions(OpenAiChatOptions.builder().model("gpt-4").temperature(0.7).build())
                    .build();

            String prompt = "What is the capital of France?";

            String groqResponse = ChatClient.builder(groqModel).build().prompt(prompt).call().content();
            String gpt4Response = ChatClient.builder(gpt4Model).build().prompt(prompt).call().content();

            log.info("Groq (Llama3) response: {}", groqResponse);
            log.info("OpenAI GPT-4 response: {}", gpt4Response);

            return List.of(Objects.requireNonNull(groqResponse), Objects.requireNonNull(gpt4Response));
        } catch (Exception e) {
            log.error("Error in multi-client flow", e);
            return null;
        }
    }
}
