# Quick Start 예제

5분 만에 Kafka 메시지 송수신을 경험하는 Spring Boot 예제입니다.

## 사전 요구사항

- Java 17+
- Docker & Docker Compose

## 실행 방법

### 1. Kafka 시작

```bash
cd docker
docker-compose up -d
```

### 2. 애플리케이션 실행

```bash
cd examples/quick-start
./gradlew bootRun
```

### 3. 메시지 전송

```bash
curl -X POST http://localhost:8080/api/messages \
  -H "Content-Type: text/plain" \
  -d "Hello Kafka!"
```

### 4. 결과 확인

애플리케이션 로그에서 다음과 같은 메시지를 확인할 수 있습니다:

```
INFO  c.e.quickstart.MessageConsumer : 메시지 수신: Hello Kafka!
```

## 종료

```bash
# 애플리케이션: Ctrl+C

# Kafka 종료
cd docker
docker-compose down
```

## 프로젝트 구조

```
quick-start/
├── build.gradle.kts          # Gradle 빌드 설정
├── src/main/java/
│   └── com/example/quickstart/
│       ├── QuickStartApplication.java  # 메인 클래스
│       ├── ProducerController.java     # REST API로 메시지 전송
│       └── MessageConsumer.java        # Kafka 메시지 수신
└── src/main/resources/
    └── application.yml       # Kafka 연결 설정
```
