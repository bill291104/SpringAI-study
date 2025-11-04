# Multimodality API

> "All things that are naturally connected ought to be taught in combination" - John Amos Comenius, "Orbis Sensualium Pictus", 1658
> 
> ~~자연스럽게 연관된 것들은 같이 배워야한다? 가르쳐야 한다?~~

인간은 여러 데이터를 동시에 처리한다. 시각, 청각, 문자만 있는 것이 아니다.

반면 머신러닝 분야에서는 특정 입력에 특화된 모델에 집중해왔다.
예를 들어 text-to-speech 나 speech-to-text 를 위한 오디오 모델 그리고 물체 인식, 물체 분류를 위한 컴퓨터 비전 모델이있다.

그러나 멀티모달 LLM 이 새로 등장했다.
많은 모델들이 글, 그림, 소리를 입력으로 받고 이를 종합해 응답을 할 수 있다.

## Spring AI Multimodality
멀티모달리티는 다양한 소스로부터 정보를 이해하고 처리할 수 있는 능력을 말한다.

Spring AI 의 Message API 는 멀티모달리티를 위한 추상클래스들을 지원한다.
![](https://docs.spring.io/spring-ai/reference/_images/spring-ai-message-api.jpg)

UserMessage 의 content 필드는 주로 텍스트 입력에 쓰고, optional 인 media 필드는 추가적으로 하나 이상의 이미지나 오디오 비디오 같은 컨텐츠를 추가할 수 있게 해준다.
MimeType 은 모달리티의 타입을 결정한다.
사용된 LLM 에 따라 Media 필드는 실제 미디어인 Resource 객체이거나 컨텐츠의 URI 가 올 수 있다.

> NOTE
> media 필드는 사용자 입력에만 사용할 수 있으며 시스템 메시지에는 소용이 없다.
> LLM 의 응답을 담고있은 AssistantMessage 도 텍스트만 올 수 있다.
> 텍스트가 아닌 미디어 출력을 생성하려면 전용 single-modality 모델을 사용해야 한다.

![](/src/main/resources/multimodal.test.png)
예를 들어, 이 이미지를 입력으로 넣고 뭐가 보이냐고 물어볼 수 있다.
코드는 이렇게 생겼을것:
```java
var imageResource = new ClassPathResource("/multimodal.test.png");

var userMessage = UserMessage.builder()
    .text("Explain what do you see in this picture?") // content
    .media(new Media(MimeTypeUtils.IMAGE_PNG, this.imageResource)) // media
    .build();

ChatResponse response = chatModel.call(new Prompt(this.userMessage));
```
또는 ChatClient API 를 쓰면:
```java
String response = ChatClient.create(chatModel).prompt()
		.user(u -> u.text("Explain what do you see on this picture?")
				    .media(MimeTypeUtils.IMAGE_PNG, new ClassPathResource("/multimodal.test.png")))
		.call()
		.content();
```

Spring AI 에서 멀티모달을 지원하는 모델들:
- Anthropic Claude 3
- AWS Bedrock Converse
- Azure Open AI (e.g. GPT-4o models)
- Mistral AI (e.g. Mistral Pixtral models)
- Ollama (e.g. LLaVA, BakLLaVA, Llama3.2 models)
- OpenAI (e.g. GPT-4 and GPT-4o models)
- Vertex AI Gemini (e.g. gemini-1.5-pro-001, gemini-1.5-flash-001 models)