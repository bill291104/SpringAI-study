# Chat Memory
LLM 은 stateless 하다.
즉 이전의 대화내용을 기억하지 않는다는 것이다.
문맥이나 상태를 유지하거나 여러 단계로 상호작용 하고 싶다면 걸림돌이 된다.
Spring AI 는 chat memory 를 제공해서 문맥을 저장할 수 있게 해준다.

ChatMemory 추상클래스는 여러 타입의 메모리를 구현하게 해준다.
메시지는 ChatMemoryRepository 에 저장된다.
어떤 메시지를 저장할지 그리고 언제 삭제할지는 ChatMemory 의 구현체가 결정한다.

chat memory 와 chat history 의 차이를 알아야 한다:
- Chat Memory: LLM 이 문맥 이해를 위해 유지하는 정보
- Chat History: 모든 대화 이력, 모델과 사용자가 주고받은 모든 메시지

ChatMemory 추상 클래스는 chat memory 를 관리하기 위한 것이다.
만일 chat history 가 필요하다면 Spring Data 같은 것을 사용하는 것이 더 적절하다.

## Quick Start

ChatMemory 는 Spring AI 에서 자동으로 빈 등록을 해준다.
어플리케이션에 바로 사용할 수 있다.
기본으로 InMemoryChatMemoryRepository 를 사용하며 MessageWindowChatMemory 구현체를 사용해 대화 내역을 관리한다.
만일 다른 저장소가 먼저 설정되었다면 대신 그것(Cassandra, JDBC, Neo4j 같은)을 사용한다.
```java
@Autowired
ChatMemory chatMemory;
```

이 뒤로는 더 자세한 내용들을 다룬다.

[//]: # (TODO 나중에 이어서 작성)
## Memory Types
