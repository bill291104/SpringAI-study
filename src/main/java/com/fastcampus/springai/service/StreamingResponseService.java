package com.fastcampus.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class StreamingResponseService {
    private final ChatClient chatClient;

    public StreamingResponseService(@Qualifier("vertexAiChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public Flux<String> streamingResponse() {
        return this.chatClient.prompt()
                .user("Tell me the entire history of the Internet, starting from ARPANET to modern AI-driven search engines, detailing key milestones, technologies (like TCP/IP and the World Wide Web), and social impacts. Provide the answer in at least five detailed paragraphs.")
                .stream()
                .content();
    }
}
