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
