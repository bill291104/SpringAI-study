package com.fastcampus.springai.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class ChatResponseEntityTypeService {
    private final ChatClient chatClient;

    public ChatResponseEntityTypeService(@Qualifier("vertexAiChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    record ActorFilms(String actor, List<String> movies) {}

    public void generateFilmography() {
        ActorFilms randomActorFilms = chatClient.prompt()
                .user("Generate the filmography for a random actor.")
                .call()
                .entity(ActorFilms.class);

       log.info(randomActorFilms);

        List<ActorFilms> actorFilms = chatClient.prompt()
                .user("Generate the filmography of 5 movies for Tom Hanks and Bill Murray.")
                .call()
                .entity(new ParameterizedTypeReference<List<ActorFilms>>() {});

        log.info(actorFilms);
    }
}
