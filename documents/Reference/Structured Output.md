# Structured Output Converter
AI 모델의 응답을 신뢰할 수 있는 안정적인 데이터 타입으로 변환하는 것은 어플리케이션 개발자에게는 중요한 작업이다.
Spring AI 의 Structured Output Converters 는 LLM 의 출력을 특정한 형식으로 변환하는 것을 도와준다.
![](https://docs.spring.io/spring-ai/reference/_images/structured-output-architecture.jpg)

공식 문서에서는 이 Converter 의 동작방식을 설명하기를, 
프롬프트에 출력 형식을 설명하는 지시문을 이어 붙이고 
그렇게 나온 모델의 출력 문자열을 파싱한다고 설명하고 있다.
그리고 소스코드를 보아도 특별한 안전장치는 보이지 않는다.
대부분의 성능 좋은 모델이라면 대체로 응답을 잘 할테지만,
개인적인 경험으로는 생성자를 Tool 로 등록하고 반드시 사용하도록 지시하거나 보장하는 것이 더 안전한 느낌이다.

## Structured Output API
StructuredOutputConverter 인터페이스 정의:
```java
public interface StructuredOutputConverter<T> extends Converter<String, T>, FormatProvider {

}
```
Spring 의 Converter<String, T> 와 FormatProvider 인터페이스가 합쳐진 형태이다.
```java
public interface FormatProvider {
	String getFormat();
}
```
![](https://docs.spring.io/spring-ai/reference/_images/structured-output-api.jpg)

FormatProvider 는 타입 매개변수 T 를 통해서 Converter 가 변환할 수 있는 출력을 내도록 모델에게 전달할 가이드라인을 제공한다.
그 지시는 이렇게 생겼다:
```
Your response should be in JSON format.
The data structure for the JSON should match this Java class: java.util.HashMap
Do not include any explanations, only provide a RFC8259 compliant JSON response following this format without deviation.
```
이것은 보통 PromptTemplate 을 사용해서 사용자 입력 뒤에 이어 붙여서 전달한다:
```java
    StructuredOutputConverter outputConverter = ...
    String userInputTemplate = """
        ... user text input ....
        {format}
        """; // user input with a "format" placeholder.
    Prompt prompt = new Prompt(
        PromptTemplate.builder()
            .template(this.userInputTemplate)
            .variables(Map.of(..., "format", this.outputConverter.getFormat())) // replace the "format" placeholder with the converter's format.
            .build().createMessage()
    );
```
~~딱 봐도 불안정해 보인다.~~

### Available Converters
![](https://docs.spring.io/spring-ai/reference/_images/structured-output-hierarchy4.jpg)

- `AbstractConversionServiceOutputConverter<T>`: 미리 설정된 GenericConversionService 를 제공한다. FormatProvider 의 기본 구현체는 제공하지 않는다.
- `AbstractMessageOutputConverter<T>`: 미리 설정된 MessageConverter 를 제공한다. FormatProvider 의 기본 구현체는 제공하지 않는다.
- `BeanOutputConverter<T>`: 빈으로 등록된 Java 클래스나 ParameterizedTypeReference 를 사용하여 설정한다. FormatProvider 를 사용해서 JSON 객체를 반환하도록 지시하고 ObjectMapper 를 사용해서 객체로 변환한다.
- `MapOutputConverter`: FormatProvider 의 구현체를 사용해서 AbstractMessageOutputConverter 를 구현한다. MessageConverter 로 맵으로 변환한다.
- `ListOutputConverter`: FormatProvider 의 구현체를 사용해서 AbstractConversionServiceOutputConverter 를 구현한다. ConversionService 로 리스트로 변환한다.

## Using Converters

### Bean Output Converter
```java
record ActorsFilms(String actor, List<String> movies) { }
```
ChatClient 사용해서 적용:
```java
ActorsFilms actorsFilms = ChatClient.create(chatModel).prompt()
        .user(u -> u.text("Generate the filmography of 5 movies for {actor}.")
                    .param("actor", "Tom Hanks"))
        .call()
        .entity(ActorsFilms.class);
```
ChatModel 사용해서 적용:
```java
BeanOutputConverter<ActorsFilms> beanOutputConverter =
    new BeanOutputConverter<>(ActorsFilms.class);

String format = this.beanOutputConverter.getFormat();

String actor = "Tom Hanks";

String template = """
        Generate the filmography of 5 movies for {actor}.
        {format}
        """;

Generation generation = chatModel.call(
    PromptTemplate.builder().template(this.template).variables(Map.of("actor", this.actor, "format", this.format)).build().create()).getResult();

ActorsFilms actorsFilms = this.beanOutputConverter.convert(this.generation.getOutput().getText());
```

### Property Ordering in Generated Schema
BeanOutputConverter 는 @JsonPropertyOrder 애너테이션을 통해서 JSON 프로퍼티의 순서를 보장할 수 있다.
```java
@JsonPropertyOrder({"actor", "movies"})
record ActorsFilms(String actor, List<String> movies) {}
```

#### Generic Bean Types
ParameterizedTypeReference 생성자를 사용해서 클래스 구조를 특정한다.
```java
List<ActorsFilms> actorsFilms = ChatClient.create(chatModel).prompt()
        .user("Generate the filmography of 5 movies for Tom Hanks and Bill Murray.")
        .call()
        .entity(new ParameterizedTypeReference<List<ActorsFilms>>() {});
```
아니면 ChatModel 을 직접 사용한다:
```java
BeanOutputConverter<List<ActorsFilms>> outputConverter = new BeanOutputConverter<>(
        new ParameterizedTypeReference<List<ActorsFilms>>() { });

String format = this.outputConverter.getFormat();
String template = """
        Generate the filmography of 5 movies for Tom Hanks and Bill Murray.
        {format}
        """;

Prompt prompt = PromptTemplate.builder().template(this.template).variables(Map.of("format", this.format)).build().create();

Generation generation = chatModel.call(this.prompt).getResult();

List<ActorsFilms> actorsFilms = this.outputConverter.convert(this.generation.getOutput().getText());
```

### Map Output Converter
```java
Map<String, Object> result = ChatClient.create(chatModel).prompt()
        .user(u -> u.text("Provide me a List of {subject}")
                    .param("subject", "an array of numbers from 1 to 9 under they key name 'numbers'"))
        .call()
        .entity(new ParameterizedTypeReference<Map<String, Object>>() {});
```
ChatModel
```java
MapOutputConverter mapOutputConverter = new MapOutputConverter();

String format = this.mapOutputConverter.getFormat();
String template = """
        Provide me a List of {subject}
        {format}
        """;

Prompt prompt = PromptTemplate.builder().template(this.template)
.variables(Map.of("subject", "an array of numbers from 1 to 9 under they key name 'numbers'", "format", this.format)).build().create();

Generation generation = chatModel.call(this.prompt).getResult();

Map<String, Object> result = this.mapOutputConverter.convert(this.generation.getOutput().getText());
```

### List Output Converter
```java
List<String> flavors = ChatClient.create(chatModel).prompt()
                .user(u -> u.text("List five {subject}")
                            .param("subject", "ice cream flavors"))
                .call()
                .entity(new ListOutputConverter(new DefaultConversionService()));
```
ChatModel
```java
ListOutputConverter listOutputConverter = new ListOutputConverter(new DefaultConversionService());

String format = this.listOutputConverter.getFormat();
String template = """
        List five {subject}
        {format}
        """;

Prompt prompt = PromptTemplate.builder().template(this.template).variables(Map.of("subject", "ice cream flavors", "format", this.format)).build().create();

Generation generation = this.chatModel.call(this.prompt).getResult();

List<String> list = this.listOutputConverter.convert(this.generation.getOutput().getText());
```

## Supported AI Models
위 기능들이 동작하는지 테스트 해본 모델들은 다음과 같다:

| Model                  | Integration Tests / Samples    |
|:-----------------------|:-------------------------------|
| **OpenAI**             | OpenAiChatModelIT              |
| **Anthropic Claude 3** | AnthropicChatModelIT.java      |
| **Azure OpenAI**       | AzureOpenAiChatModelIT.java    |
| **Mistral AI**         | MistralAiChatModelIT.java      |
| **Ollama**             | OllamaChatModelIT.java         |
| **Vertex AI Gemini**   | VertexAiGeminiChatModelIT.java |

## Built-in JSON mode
몇몇 모델은 전용 옵션이 있다.

- OpenAI Structured Outputs: JSON_OBJECT 나 JSON_SCHEMA 중에 선택가능(`spring.ai.openai.chat.options.responseFormat` 옵션)
- Azure OpenAI: `spring.ai.azure.openai.chat.options.responseFormat` 을 설정 가능하다. { "type": "json_object" } 로 설정해서 JSON 을 반환하게 한다.
- Ollama: `spring.ai.ollama.chat.options.format` 을 설정 가능하다. 지금은 json 이 유일한 옵션이다.
- Mistral AI: `spring.ai.mistralai.chat.options.responseFormat` 을 설정 가능하다. { "type": "json_object" } 로 설정해서 JSON 을 반환하게 한다.