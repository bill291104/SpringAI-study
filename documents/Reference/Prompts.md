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

#### Roles
메시지마다 역할이 부여되어있고 이로인해 AI 모델과의 소통을 원활하게 한다.

- System Role: AI 의 행동과 응답 스타일을 지시한다. 대화전 AI 에게 지침을 전달하는 것과 비슷하다.
- User Role: 사용자 입력이다. 이것을 기반으로 응답을 생성하기에 가장 중요하다.
- Assistant Role: AI 의 응답이다. 맥락을 유지하는데 중요하며, 단순 문자열 뿐만 아니라 Function Tool Call 도 포함될 수 있다.
- Tool/Function Role: Tool Call Assistant Message 의 응답으로, 추가 정보를 제공한다.

역할은 Sping AI 에서 열거형 타입으로 제공된다.
```java
public enum MessageType {

	USER("user"),

	ASSISTANT("assistant"),

	SYSTEM("system"),

	TOOL("tool");

    ...
}
```

### PromptTemplate
구조화된 프롬프트를 생성하는 역할.
```java
public class PromptTemplate implements PromptTemplateActions, PromptTemplateMessageActions {

    // Other methods to be discussed later
}
```
이 클래스는 TemplateRenderer API 를 사용하며 Spring AI 의 기본 구현체는 StTemplateRenderer 이다.
기본적으로 변수를 {} 통해 정의하며, 설정을 통해 구분자를 변경할 수 있다.
```java
public interface TemplateRenderer extends BiFunction<String, Map<String, Object>, String> {

	@Override
	String apply(String template, Map<String, Object> variables);

}
```
TemplateRenderer 인터페이스는 템플릿 안의 변수를 관리한다.
기본 StringTemplate 의 구현체말고, 로직을 변경해야 한다면 직접 구현체를 전달해도 된다.
템플릿 렌더링이 필요없는 경우에는 제공되는 NoOpTemplateRenderer 를 사용하면 된다.
```java
PromptTemplate promptTemplate = PromptTemplate.builder()
    .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
    .template("""
            Tell me the names of 5 movies whose soundtrack was composed by <composer>.
            """)
    .build();

String prompt = promptTemplate.render(Map.of("composer", "John Williams"));
```
구현하고있는 인터페이스는 프롬프트를 만드는 각기 다른 관점을 제공한다.
PromptTemplateStringActions 는 문자열 프롬프트를 만들고 렌더링하는데 집중한다.
PromptTemplateMessageActions 는 Message 객체를 생성하고 조정해서 프롬프트를 생성하는데 최적화 되어있다.
PromptTemplateActions 는 Prompt 객체를 반환하여 ChatModel 에게 전달될 수 있도록 한다.
```java
public interface PromptTemplateStringActions {

	String render();

	String render(Map<String, Object> model);

}
```
String render() 메서드: 외부 입력 없이 프롬프트 템플릿을 최종 문자열 형태로 반환한다.
placeholder 나 동적인 요소가 없을 때 적합하다.

String render(Map<String, Object> model) 메서드: 동적인 요소를 포함하고 있다.
맵의 키가 placeholder 이름이고 값이 동적으로 삽입될 요소이다.
```java
public interface PromptTemplateMessageActions {

	Message createMessage();

    Message createMessage(List<Media> mediaList);

	Message createMessage(Map<String, Object> model);

}
```
Message createMessage() 메서드: 추가적인 데이터 없이 Message 객체를 생성한다.
고정 메시지나 사전 정의 메시지에 사용한다.

Message createMessage(List<Media> mediaList) 메서드: Message 객체를 문자와 미디어 요소와 함께 생성한다.

Message createMessage(Map<String, Object> model) 메서드: 메시지 생성을 동적으로 확장하여 맵을 받는다.
맵의 각 쌍은 placeholder 의 이름과 동적인 값이다.
```java
public interface PromptTemplateActions extends PromptTemplateStringActions {

	Prompt create();

	Prompt create(ChatOptions modelOptions);

	Prompt create(Map<String, Object> model);

	Prompt create(Map<String, Object> model, ChatOptions modelOptions);

}
```
Prompt create() 메서드: 추가적인 데이터 없이 Prompt 객체를 생성한다.
고정 프롬프트나 사전 정의 프롬프트에 사용하는 것이 이상적이다.

Prompt create(ChatOptions modelOptions) 메서드: chat request 를 위한 특정 옵션을 통해 Prompt 객체를 생성한다.

Prompt create(Map<String, Object> model) 메서드: 프롬프트 생성에 동적 기능을 확장했다.

Prompt create(Map<String, Object> model, ChatOptions modelOptions): 위 두 메서드를 한번에 수행한다.

## Example Usage
PromptTemplate 예시
```java
PromptTemplate promptTemplate = new PromptTemplate("Tell me a {adjective} joke about {topic}");

Prompt prompt = promptTemplate.create(Map.of("adjective", adjective, "topic", topic));

return chatModel.call(prompt).getResult();
```
역할 예시
```java
String userText = """
    Tell me about three famous pirates from the Golden Age of Piracy and why they did.
    Write at least a sentence for each pirate.
    """;

Message userMessage = new UserMessage(userText);

String systemText = """
  You are a helpful AI assistant that helps people find information.
  Your name is {name}
  You should reply to the user's request with your name and also in the style of a {voice}.
  """;

SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemText);
Message systemMessage = systemPromptTemplate.createMessage(Map.of("name", name, "voice", voice));

Prompt prompt = new Prompt(List.of(userMessage, systemMessage));

List<Generation> response = chatModel.call(prompt).getResults();
```

### Using a custom template renderer
TemplateRenderer 인터페이스를 구현해서 PromptTemplate 생성자에 전달할 수 있다.
기본인 StTemplateRenderer 를 사용하되 설정을 커스텀 할 수도 있다.
기본 구분자는 `{}` 이지만 JSON 을 템플릿에 사용할거라면 `<>` 같은 것으로 대체할 수 있다.
```java
PromptTemplate promptTemplate = PromptTemplate.builder()
    .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
    .template("""
            Tell me the names of 5 movies whose soundtrack was composed by <composer>.
            """)
    .build();

String prompt = promptTemplate.render(Map.of("composer", "John Williams"));
```

### Using resources instead of raw Strings
Spring AI 는 `org.springframework.core.io.Resource` 추상클래스를 제공한다.
이것으로 PromptTemplate 에 데이터 파일을 바로 사용 가능하다.
```java
@Value("classpath:/prompts/system-message.st")
private Resource systemResource;
```
```java
SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemResource);
```

## Prompt Engineering
AI 에서 프롬프트를 잘 생성하는 작업은 중요하고 어려운 작업이다.

### Creating effective prompts
프롬프트에는 중요한 핵심 요소들이 있다:
- Instructions: 어떻게 소통할 것인지와 같은 명확하고 직접적인 지시를 준다. AI 는 무엇이 기대되는지 이해할 수 있다.
- External Context: AI 응답에 대한 배경지식이나 특정한 가이드이다. 
- User Input: 사용자의 직접적인 요청이다.
- Output Indicator: 요구되는 특정한 출력 형식이다. 이 지시를 무조건 따르지는 않는다는 것을 주의해야 한다.

다른 몇가지 기술들도 있다.

간단한 기술:
- Text Summarization
- Question Answering
- Text Classification
- Conversation
- Code Generation

심화 기술:
- Zero-shot, Few-shot Learning
- Chain-of-Thought
- ReAct (Reason + Act)

Microsoft Guidance:
- Framework for Prompt Creation and Optimization

## Tokens
토큰은 모델의 문자 처리 작업에서 다리 역할을 한다.
입력 문자열이 토큰이 됐다가 출력 토큰이 최종 응답 문자열이 된다.

토큰은 기술적인 부분 뿐만 아니라 돈에 연관되어 있다:
- Billing: AI 서비스는 보통 토큰 사용량으로 비용을 결정한다. 출력 입력 모두 해당하므로 프롬프트를 짧게 만드는것이 경제적이다.
- Model Limits: context window 라고 한번에 처리하고 이해할 수 있는 정보의 최대 양을 가지고있다. 
- Context Window: 이 제한을 초과하는 입력은 처리되지 않으니 최대한 작고 효율적인 입력을 주는것이 중요하다. 헴릿을 이야기한다면 셰익스피어의 다른 작품에 대한 이야기는 할 필요가 없다는 것이다.
- Response Metadata: 모델 응답의 메타데이터에 사용한 토큰 수가 나온다.