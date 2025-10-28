# Prompts
프롬프트는 AI 모델이 특정 출력을 생성하도록 하는 입력이다.
Spring AI 에서 프롬프트를 다루는 방식은 Spring MVC 에서 View 를 다루는 방식과 유사하다.
Placeholder 를 통해 사용자 입력이나 애플리케이션 코드에 따라 동적으로 문장을 만들 수 있다.
OpenAI 가 설계한 역할로 구분된(system, user, assistant) 프롬프트가 오늘날의 프롬프트의 포준처럼 되었다.(대부분의 모델이 이 구조로 지도학습 되어있다.)

## API Overview

### Prompt
보통 ChatModel 클래스의 call() 메서드를 통해 Prompt 인스턴스를 넘겨주고 ChatResponse 를 받는 형태로 사용한다.
Prompt 는 Message 객체와 요청의 ChatOptions 를 포함하는 구조화된 클래스이다.
Message 마다 역할이 부여되어 있고 의도가 다 다르다.
Prompt 클래스는 대충 이렇게 생겼다:
```java
public class Prompt implements ModelRequest<List<Message>> {

    private final List<Message> messages;

    private ChatOptions chatOptions;
}
```

### Message
Message 인터페이스는 Prompt, 메타데이터, MessageType 을 캡슐화한다.
```java
public interface Content {

	String getContent();

	Map<String, Object> getMetadata();
}

public interface Message extends Content {

	MessageType getMessageType();
}
```
여러 메시지 타입을 지원하는 MediaContent 인터페이스는 Media 객체의 List 를 제공한다.
```java
public interface MediaContent extends Content {

	Collection<Media> getMedia();

}
```
메시지는 역할에 따라 구현체가 각각 존재한다.
![](https://docs.spring.io/spring-ai/reference/_images/spring-ai-message-api.jpg)
