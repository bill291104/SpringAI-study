# SpringAI

> SpringAI Version: 1.0.2
> 
> [Reference Doc.](https://docs.spring.io/spring-ai/reference/index.html)
> 
> [API Doc.](https://docs.spring.io/spring-ai/docs/1.0.2/api/)

## Overview
### AI Concepts
#### Models
SpringAI 는 여러 형태의 모델을 지원하지만, 특히나 Embedding 모델에 초점이 맞춰져 있다.
#### Prompts
모델에 입력으로 주어지는 prompt 를 다루기 위해 
StringTemplate 인터페이스를 사용한다.
Prompt 에 있는 placeholder 에 내용을 채워서 모델에 전달된다는 관점에서
Spring MVC 의 View 와 일맥상통하는 개념이다.
#### Embeddings
#### Tokens
#### Structured Output
원래 모델의 출력은 문자열이고, JSON 으로 달라고 지시 해봤자 정확하지 않을 수 있으며,
정확하더라도 그것은 JSON 형식의 문자열이다.
그래서 단순 문자열을 어플리케이션에서 다루는 데이터 구조로 변환하는 기술이 생겨났다
#### Bringing Your Data & APIs to the AI Model
학습하지 않은 데이터에 대해 모델은 알 방법이 없다.
이를 해결하는 기술은 3가지가 있다:
1. Fine-Tuning
2. Prompt Stuffing(RAG)
3. Tool Calling
#### Evaluating AI responses
일반적인 ML 에서 이야기하는 Evaluation 과 다르게,
SpringAI 에서의 Evaluation 은 모델의 출력을 검증하는 개념이다.
Evaluator 인터페이스의 구현체인 RelevancyEvaluator 와 FactCheckingEvaluator 는
모델의 출력이 사용자 입력과 문맥이 일치하는지와 hallucination 이 발생하지 않았는지와 같은 검증을 해준다.
테스트 코드 작성시에 유용하다고 설명하고 있다.

## Reference
### Chat Client API
ChatClient 는 AI 모델과 의사소통 할 수 있는 API 이다.
AI 모델의 입력으로 전달할 Prompt 의 요소를 구성할 수 있는 메서드를 가지고 있다.
프롬프트는 메시지(문자열) 컬렉션으로 이루어져 있다.
메시지는 runtime 에 커스터마이징 가능하도록 placeholder 를 가질 수 있다.
사용할 모델 이름이나 temperature 같은 Prompt 옵션도 있다.

#### Creating a ChatClient
ChatClient 객체는 ChatClient.Builder 객체로 생성할 수 있다.
##### Using an autoconfigured ChatClient.Builder
Spring AI 는 Spring Boot autoconfiguration 을 제공한다.
ChatClient.Builder 빈을 주입하여 사용하는 것이 가장 간단한 방법이다.
```java
@RestController
class MyController {

    private final ChatClient chatClient;

    public MyController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/ai")
    String generation(String userInput) {
        return this.chatClient.prompt()
            .user(userInput)
            .call()
            .content();
    }
}
```
##### Working with Multiple Chat Models
하나의 어플리케이션에서 여러 모델을 사용해야하는 경우가 있다.
기본적으로 Spring AI autoconfigures 는 ChatClient.Builder 빈 하나만 제공한다.
그때는 다음과 같은 설정이 필요하다:
`spring.ai.chat.client.enabled=false` 로 ChatClient.Builder autoconfiguration 을 비활성화한다.
이렇게 하면 ChatClient 인스턴스를 수동으로 여러개 만들 수 있다.
##### Multiple ChatClients with a Single Model Type
같은 모델이지만 다른 설정을 사용하는 여러개의 인스턴스를 생성하려면:
```java
// Create ChatClient instances programmatically
ChatModel myChatModel = ... // already autoconfigured by Spring Boot
ChatClient chatClient = ChatClient.create(myChatModel);

// Or use the builder for more control
ChatClient.Builder builder = ChatClient.builder(myChatModel);
ChatClient customChatClient = builder
    .defaultSystem("You are a helpful assistant.")
    .build();
```
##### ChatClients for Different Model Types
모델이 여러개라면 각각 ChatClient 빈을 선언할 수 있다:
```java
import org.springframework.ai.chat.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient openAiChatClient(OpenAiChatModel chatModel) {
        return ChatClient.create(chatModel);
    }

    @Bean
    public ChatClient anthropicChatClient(AnthropicChatModel chatModel) {
        return ChatClient.create(chatModel);
    }
}
```
이렇게 하고 `@Qualifier` 애너테이션으로 주입하면 된다:
```java
@Configuration
public class ChatClientExample {

    @Bean
    CommandLineRunner cli(
            @Qualifier("openAiChatClient") ChatClient openAiChatClient,
            @Qualifier("anthropicChatClient") ChatClient anthropicChatClient) {

        return args -> {
            var scanner = new Scanner(System.in);
            ChatClient chat;

            // Model selection
            System.out.println("\nSelect your AI model:");
            System.out.println("1. OpenAI");
            System.out.println("2. Anthropic");
            System.out.print("Enter your choice (1 or 2): ");

            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                chat = openAiChatClient;
                System.out.println("Using OpenAI model");
            } else {
                chat = anthropicChatClient;
                System.out.println("Using Anthropic model");
            }

            // Use the selected chat client
            System.out.print("\nEnter your question: ");
            String input = scanner.nextLine();
            String response = chat.prompt(input).call().content();
            System.out.println("ASSISTANT: " + response);

            scanner.close();
        };
    }
}
```
##### Multiple OpenAI-Compatible API Endpoints
OpenAiApi 와 OpenAiChatModel 클래스는 mutate() 메서드를 제공해서 존재하는 인스턴스에 여러가지 설정을 적용할 수 있게 해준다.
OpenAI 와 호환되는 API 들을 다룰 때 특히 유용하다.(사실상 OpenAI 의 인터페이스가 표준처럼 사용되며 대부분의 모델과 호환된다.)
```java
@Service
public class MultiModelService {

    private static final Logger logger = LoggerFactory.getLogger(MultiModelService.class);

    @Autowired
    private OpenAiChatModel baseChatModel;

    @Autowired
    private OpenAiApi baseOpenAiApi;

    public void multiClientFlow() {
        try {
            // Derive a new OpenAiApi for Groq (Llama3)
            OpenAiApi groqApi = baseOpenAiApi.mutate()
                .baseUrl("https://api.groq.com/openai")
                .apiKey(System.getenv("GROQ_API_KEY"))
                .build();

            // Derive a new OpenAiApi for OpenAI GPT-4
            OpenAiApi gpt4Api = baseOpenAiApi.mutate()
                .baseUrl("https://api.openai.com")
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .build();

            // Derive a new OpenAiChatModel for Groq
            OpenAiChatModel groqModel = baseChatModel.mutate()
                .openAiApi(groqApi)
                .defaultOptions(OpenAiChatOptions.builder().model("llama3-70b-8192").temperature(0.5).build())
                .build();

            // Derive a new OpenAiChatModel for GPT-4
            OpenAiChatModel gpt4Model = baseChatModel.mutate()
                .openAiApi(gpt4Api)
                .defaultOptions(OpenAiChatOptions.builder().model("gpt-4").temperature(0.7).build())
                .build();

            // Simple prompt for both models
            String prompt = "What is the capital of France?";

            String groqResponse = ChatClient.builder(groqModel).build().prompt(prompt).call().content();
            String gpt4Response = ChatClient.builder(gpt4Model).build().prompt(prompt).call().content();

            logger.info("Groq (Llama3) response: {}", groqResponse);
            logger.info("OpenAI GPT-4 response: {}", gpt4Response);
        }
        catch (Exception e) {
            logger.error("Error in multi-client flow", e);
        }
    }
}
```

#### ChatClient Fluent API
ChatClient API 는 오버로딩된 prompt 메서드로 프롬프트를 작성하는 3가지 방식을 지원한다.

- `prompt()`: API 를 사용할 준비. user, system 등 프롬프트의 요소를 build 할 수 있다.
- `prompt(Prompt prompt)`: Prompt 인스턴스를 직접 만들어서 전달할 수 있다.
- `prompt(String content)`: user 의 입력을 받아들인다.

#### ChatClient Responses
ChatClient API 는 AI 모델의 응답 형태를 지정할 수 있는 여러개의 방식을 제공한다.
##### Returning a ChatResponse
AI 모델의 응답인 ChatResponse 타입은 여러 중요한 메타데이터(토큰 개수 등)를 포함하고 있다.
`call()` 메서드 이후 `chatResponse()` 메서드를 호출하면 ChatResponse 객체를 반환한다.
```java
ChatResponse chatResponse = chatClient.prompt()
    .user("Tell me a joke")
    .call()
    .chatResponse();
```
##### Returning an Entity
반환된 문자열을 매핑한 Entity 클래스를 원할 수도 있다.
`entity()` 메서드가 이 기능을 한다.
```java
record ActorFilms(String actor, List<String> movies) {}
```
```java
ActorFilms actorFilms = chatClient.prompt()
    .user("Generate the filmography for a random actor.")
    .call()
    .entity(ActorFilms.class);
```
오버로딩된 `entity(ParameterizedTypeReference<T> type)` 메서드로 특정한 타입, 예를 들어 제네릭 리스트를 지정할 수 있다:
```java
List<ActorFilms> actorFilms = chatClient.prompt()
        .user("Generate the filmography of 5 movies for Tom Hanks and Bill Murray.")
        .call()
        .entity(new ParameterizedTypeReference<List<ActorFilms>>() {});
```
##### Streaming Responses
`stream()` 메서드로 비동기 응답을 받을 수 있다.
```java
Flux<String> output = chatClient.prompt()
    .user("Tell me a joke")
    .stream()
    .content();
```
`Flux<ChatResponse> chatResponse()` 메서드로 ChatResponse 도 stream(비동기 처리) 할 수 있다.

추후에는 stream() 메서드가 Java entity 를 반환하는 편의 메서드를 제공할 계획이라고 한다.
하지만 지금은 Structured Output Converter 를 사용해야한다.
```java
var converter = new BeanOutputConverter<>(new ParameterizedTypeReference<List<ActorsFilms>>() {});

Flux<String> flux = this.chatClient.prompt()
    .user(u -> u.text("""
                        Generate the filmography for a random actor.
                        {format}
                      """)
            .param("format", this.converter.getFormat()))
    .stream()
    .content();

String content = this.flux.collectList().block().stream().collect(Collectors.joining());

List<ActorsFilms> actorFilms = this.converter.convert(this.content);
```

#### Prompt Templates
ChatClient API 는 user 와 system 의 내용을 템플릿처럼 변수를 넣어서 runtime 에 바꿔치기 가능하게 해준다.
```java
String answer = ChatClient.create(chatModel).prompt()
    .user(u -> u
            .text("Tell me the names of 5 movies whose soundtrack was composed by {composer}")
            .param("composer", "John Williams"))
    .call()
    .content();
```
내부적으로 ChatClient 는 PromptTemplate 클래스를 사용하며 TemplateRenderer 의 구현체를 사용하여 변수를 체워넣는다.
Spring AI 는 StringTemplate 엔진의 구현체인 StTemplateRenderer 를 사용하며, 템플릿 작업이 필요없는 경우를 위해 NoOpTemplateRenderer 도 제공한다.

> Note
> ChatClient 에서 (.templateRenderer() 를 통해)적용한 TemplateRenderer 설정은 ChatClient 의 builder chain 의 프롬프트 요소에만 적용된다.
> QuestionAnswerAdvisor 같은 Advisor 에는 따로 설정해줘야 한다.

기본적으로 StTemplateRenderer 를 사용하면서 커스텀 설정도 적용할 수 있다.
예를 들어, 템플릿 변수는 `{}`로 표현하는 문법을 사용하는데 JSON 을 프롬프트에 넣어야 하는 상황이 생길 수 있다.
JSON 문법과 충돌을 피하기 위해 다른 규칙으로 변경해서 해결할 수 있다. 예를 들어 `<` 와 `>` 를 사용한다면:
```java
String answer = ChatClient.create(chatModel).prompt()
    .user(u -> u
            .text("Tell me the names of 5 movies whose soundtrack was composed by <composer>")
            .param("composer", "John Williams"))
    .templateRenderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
    .call()
    .content();
```
~~python 으로 프롬프트 빌드할 때 겪었던 문제인데 SpringAI 에서 깔끔하게 해결한 것 같아 반갑다.~~

#### call() return values
ChatClient 의 `call()` 이후 반환 타입을 위한 여러 옵션이 있다.

- `String content()`: 문자열 응답 반환.
- `ChatResponse chatResponse()`: 여러개의 응답과 메타데이터를 포함한 ChatResponse 객체 반환.
- `ChatClientResponse chatClientResponse()`: ChatResponse 객체와, advisor 가 수행하면서 사용한 데이터를 포함한 ChatClientResponse 객체 반환.
- `ResponseEntity<?> responseEntity()`: 완전한 HTTP 응답을 반환. HTTP 응답을 low-level 로 다뤄야 한다면 유용함.
- Java 타입을 반환하기 위한 `entity()`
    - `entity(ParameterizedTypeResference<T> type)`: 엔티티 타입의 컬렉션을 반환할 때 사용.
    - `entity(Class<T> type)`: 특정한 엔티티 타입을 반환할 때 사용.
    - `entity(StructuredOutputConverter<T> structuredOutputConverter)`: 문자열을 엔티티 타입으로 변환할 때 사용.

> Note
> call() 메서드를 호출한다고 실제로 AI model 이 동작하는게 아니다.
> 대신 SpringAI 에게 동기 호출인지 비동기 호출인지만 알려준다.
> 실제로 AI model 이 동작하는 시점은 content(), chatResponse(), responseEntity() 같은 메서드가 호출될 때이다.

#### stream() return values
ChatClient 의 `stream()` 이후 반환 타입을 위한 여러 옵션이 있다.

- `Flux<String> content()`: 문자열의 Flux 를 반환.
- `Flux<ChatResponse> chatResponse()`: ChatResponse 의 Flux 를 반환.
- `Flux<ChatClientResponse> chatClientResponse()`: ChatClientResponse 의 Flux 를 반환.

#### Using Defaults
`@Configuration` 클래스에 system text 를 넣어놓고 ChatClient 를 생성하면 runtime 코드가 간결해진다.
매 요청마다 system text 를 넣어줄 필요 없이, user text 만 호출 할 때 넣어주면 된다.
##### Default System Text
runtime 코드에 system text 가 반복되는 것을 피하기 위해 `@Configuration` 클래스에서 ChatClient 를 생성한다.
```java
@Configuration
class Config {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultSystem("You are a friendly chat bot that answers question in the voice of a Pirate")
                .build();
    }

}
```
그리고 `@RestController` 에서 사용:
```java
@RestController
class AIController {

	private final ChatClient chatClient;

	AIController(ChatClient chatClient) {
		this.chatClient = chatClient;
	}

	@GetMapping("/ai/simple")
	public Map<String, String> completion(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
		return Map.of("completion", this.chatClient.prompt().user(message).call().content());
	}
}
```
##### Default System Text with parameters
placeholder 를 사용할 수도 있다.
```java
@Configuration
class Config {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultSystem("You are a friendly chat bot that answers question in the voice of a {voice}")
                .build();
    }

}
```
```java
@RestController
class AIController {
	private final ChatClient chatClient;

	AIController(ChatClient chatClient) {
		this.chatClient = chatClient;
	}

	@GetMapping("/ai")
	Map<String, String> completion(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message, String voice) {
		return Map.of("completion",
				this.chatClient.prompt()
						.system(sp -> sp.param("voice", voice))
						.user(message)
						.call()
						.content());
	}

}
```
##### Other defaults
ChatClient.Builder 레벨에서 프롬프트 기본 설정을 조정할 수 있다.

- `defaultOptions(ChatOptions chatOptions)`: ChatOptions 클래스나 특정 모델의 옵션 클래스(예를 들어 OpenAiChatOptions)를 통째로 넘겨줄 수 있다.
- `defaultFunction(String name, String description, Function<I, O> function)`: name 은 user text 에서 명시한 이름, description 은 AI model 이 올바른 함수를 호출하게 도와주는 설명, function 은 필요시 모델이 실행할 Java 함수형 인터페이스.
- `defaultUser(String text)`, `defaultUser(Resource text)`, `defaultUser(Consumer<UserSpec> userSpecConsumer)`: user text 혹은 관련 파라미터를 함께 정의.
- `defaultAdvisors(Advisor... advisor)`: Advisor 는 Prompt 를 만들 때 사용하는 데이터를 변환할 수 있게 해준다. QuestionAnswerAdvisor 구현체는 RAG 에서 관련 정보를 user text 에 이어 붙인다.
- `defaultAdvisors(Consumer<AdvisorSpec> advisorSpecConsumer)`: 좀더 복잡한 Advisor 설정을 위해 사용.

default 접두사 없는 대응되는 메서드를 사용해서 런타임에 기본값을 덮어쓰기 할 수 있다.

- `optoins(ChatOptions chatOptions)`
- `function(String name, String description, Function<I, O> function)`
- `functions(String... functionNames)`
- `user(String text), user(Resource text), user(Consumer<UserSpec> userSpecConsumer)`
- `advisors(Advisor... advisor)`
- `advisors(Consumer<AdvisorSpec> advisorSpecConsumer)`

#### Advisors
Advisor API 는 Spring 어플리케이션과 AI 와의 상호작용을 조정하는 기능을 제공한다.
보통 AI 모델을 호출할 때 user text 는 프롬프트에 이어붙이거나 문맥을 강화하는데 사용된다. 
문맥이라는 데이터는 보통 다음과같은 타입이 있다:
- **나만의 데이터**: AI 모델이 학습한적 없는 데이터. 
- **대화 내역**: chat model API 는 stateless 이다. 따라서 대화 내역을 전부 모델에게 전달해야 문맥을 파악할 수 있다.

##### Advisor Configuration in ChatClient
ChatClient API 는 AdvisorSpec 인터페이스를 제공한다.
이 인터페이스는 파라미터를 추가하거나 체인에 여러 advisor 를 추가하는 메서드를 제공한다.
```java
interface AdvisorSpec {
    AdvisorSpec param(String k, Object v);
    AdvisorSpec params(Map<String, Object> p);
    AdvisorSpec advisors(Advisor... advisors);
    AdvisorSpec advisors(List<Advisor> advisors);
}
```

> IMPORTANT
> 실행되는 순서가 정해지기 때문에 advisor 를 추가하는 순서가 중요하다.
> 이전 advisor 가 만든 차이가 그대로 다음 체인으로 전달된다.

```java
ChatClient.builder(chatModel)
    .build()
    .prompt()
    .advisors(
        MessageChatMemoryAdvisor.builder(chatMemory).build(),
        QuestionAnswerAdvisor.builder(vectorStore).build()
    )
    .user(userText)
    .call()
    .content();
```
이 설정대로면 MessageChatMemoryAdvisor 가 먼저 실행되고 그다음 QuestionAnswerAdvisor 가 실행된다.

##### RAG
RAG 문서를 참고

##### Logging
SimpleLoggerAdvisor 는 ChatClient 의 request 와 response 데이터를 로깅해준다.
AI 모니터링이나 디버깅에 유용하다.

> TIP
> Spring AI 는 LLM 이나 vector store 의 상호작용을 관찰할 수 있도록 지원한다.
> Observability 문서를 참고

로깅을 활성화하려면 ChatClient 를 생성할 때 SimpleLoggerAdvisor 를 advisor 체인에 추가하면 된다.
로깅하는 advisor 는 다른 advisor 을 모두 추가하고 가장 마지막에 추가하는게 좋다.
```java
ChatResponse response = ChatClient.create(chatModel).prompt()
        .advisors(new SimpleLoggerAdvisor())
        .user("Tell me a joke?")
        .call()
        .chatResponse();
```
로그를 보려면 로깅 레벨을 DEBUG 로 하면 된다.
```
logging.level.org.springframework.ai.chat.client.advisor=DEBUG
```
AdvisedRequest 나 ChatResponse 에서 어떤 데이터를 로깅할지 커스텀 할 수도 있다.
이 생성자를 사용:
```java
SimpleLoggerAdvisor(
    Function<ChatClientRequest, String> requestToString,
    Function<ChatResponse, String> responseToString,
    int order
)
```
```java
SimpleLoggerAdvisor customLogger = new SimpleLoggerAdvisor(
    request -> "Custom request: " + request.prompt().getUserMessage(),
    response -> "Custom response: " + response.getResult(),
    0
);
```
> TIP
> 실제 프로덕션 환경에서는 민감 정보 로깅에 주의

#### Chat Memory
ChatMemory 인터페이스는 대화 내용 기억을 위한 저장공간이다.
메시지를 추가, 조회, 삭제 하는 메서드를 제공한다.
지금은 구현체가 MessageWindowChatMemory 하나뿐이다.
MessageWindowChatMemory 는 메시지 윈도우를 특정 최대 크기(기본 20)로 관리해준다.
윈도우 사이즈를 넘어가면 오래된 메시지 부터 삭제되지만 시스템 메시지는 남아있다.
시스템 메시지가 업데이트되면 이전 시스템 메시지는 삭제된다.
이렇게 하면 메모리 사용은 제한하면서 문맥의 최신화는 보장할 수 있다.
MessageWindowChatMemory 는 ChatMemoryRepository 추상클래스에 의존한다.
그 구현체는 InMemoryChatMemoryRepository, JdbcChatMemoryRepository, CassandraChatMemoryRepository, Neo4jChatMemoryRepository 등이 있다.
