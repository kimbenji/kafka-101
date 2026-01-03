---
title: "기본 예제"
weight: 2
---

# 기본 Producer/Consumer 예제

Spring Kafka를 사용한 기본적인 메시지 송수신 구현을 설명합니다.

## Producer 구현

### KafkaTemplate 주입

```java
@Service
public class MessageProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public MessageProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
}
```

> Spring Boot가 자동으로 `KafkaTemplate`을 생성하여 주입합니다.

### 동기 전송

```java
public void sendSync(String topic, String message) {
    try {
        SendResult<String, String> result = kafkaTemplate.send(topic, message).get();

        RecordMetadata metadata = result.getRecordMetadata();
        log.info("전송 완료 - Topic: {}, Partition: {}, Offset: {}",
                metadata.topic(),
                metadata.partition(),
                metadata.offset());
    } catch (Exception e) {
        log.error("전송 실패", e);
        throw new RuntimeException("메시지 전송 실패", e);
    }
}
```

```mermaid
sequenceDiagram
    participant App as Application
    participant KT as KafkaTemplate
    participant K as Kafka

    App->>KT: send(topic, message)
    KT->>K: 메시지 전송
    K-->>KT: ACK
    KT-->>App: SendResult (블로킹)
```

### 비동기 전송

```java
public void sendAsync(String topic, String message) {
    CompletableFuture<SendResult<String, String>> future =
            kafkaTemplate.send(topic, message);

    future.whenComplete((result, ex) -> {
        if (ex == null) {
            log.info("전송 성공: {}", result.getRecordMetadata().offset());
        } else {
            log.error("전송 실패", ex);
        }
    });
}
```

```mermaid
sequenceDiagram
    participant App as Application
    participant KT as KafkaTemplate
    participant K as Kafka

    App->>KT: send(topic, message)
    KT-->>App: CompletableFuture (즉시 반환)
    App->>App: 다른 작업 계속

    KT->>K: 메시지 전송
    K-->>KT: ACK
    KT-->>App: 콜백 실행
```

### Key와 함께 전송

```java
public void sendWithKey(String topic, String key, String message) {
    kafkaTemplate.send(topic, key, message);
}
```

**Key 사용 시 장점:**
- 동일 Key는 동일 Partition으로 전송
- 순서 보장 필요 시 필수

### 특정 Partition으로 전송

```java
public void sendToPartition(String topic, int partition, String key, String message) {
    kafkaTemplate.send(topic, partition, key, message);
}
```

## Consumer 구현

### 기본 @KafkaListener

```java
@Component
public class MessageConsumer {

    @KafkaListener(topics = "my-topic", groupId = "my-group")
    public void consume(String message) {
        log.info("메시지 수신: {}", message);
    }
}
```

### ConsumerRecord로 수신

```java
@KafkaListener(topics = "my-topic")
public void consume(ConsumerRecord<String, String> record) {
    log.info("Topic: {}", record.topic());
    log.info("Partition: {}", record.partition());
    log.info("Offset: {}", record.offset());
    log.info("Key: {}", record.key());
    log.info("Value: {}", record.value());
    log.info("Timestamp: {}", record.timestamp());
}
```

### 여러 Topic 구독

```java
@KafkaListener(topics = {"topic-a", "topic-b", "topic-c"})
public void consumeMultiple(String message) {
    log.info("메시지 수신: {}", message);
}
```

### 패턴으로 구독

```java
@KafkaListener(topicPattern = "order-.*")
public void consumePattern(String message) {
    // order-created, order-paid, order-shipped 등 모두 수신
    log.info("주문 이벤트: {}", message);
}
```

### 배치 수신

```java
@KafkaListener(topics = "my-topic", batch = "true")
public void consumeBatch(List<String> messages) {
    log.info("배치 수신: {}건", messages.size());
    for (String message : messages) {
        process(message);
    }
}
```

## 수동 Offset 커밋

### 설정

```yaml
spring:
  kafka:
    consumer:
      enable-auto-commit: false
    listener:
      ack-mode: manual
```

### 구현

```java
@KafkaListener(topics = "my-topic")
public void consume(String message, Acknowledgment ack) {
    try {
        // 비즈니스 로직 처리
        processMessage(message);

        // 처리 성공 시 커밋
        ack.acknowledge();
    } catch (Exception e) {
        // 처리 실패 시 커밋하지 않음 → 재처리됨
        log.error("처리 실패", e);
    }
}
```

```mermaid
flowchart TB
    A[메시지 수신] --> B{처리 성공?}
    B -->|Yes| C[acknowledge]
    B -->|No| D[커밋 안함]
    C --> E[다음 메시지]
    D --> F[재시작 시 재처리]
```

## 에러 처리

### ErrorHandler 설정

```java
@Configuration
public class KafkaConfig {

    @Bean
    public DefaultErrorHandler errorHandler() {
        return new DefaultErrorHandler(
            new FixedBackOff(1000L, 3L)  // 1초 간격, 3회 재시도
        );
    }
}
```

### @RetryableTopic 사용

```java
@RetryableTopic(
    attempts = "3",
    backoff = @Backoff(delay = 1000, multiplier = 2)
)
@KafkaListener(topics = "my-topic")
public void consume(String message) {
    // 실패 시 자동 재시도
    // 3회 실패 시 my-topic-dlt로 이동
    processMessage(message);
}
```

### Dead Letter Topic (DLT)

```mermaid
flowchart LR
    A[my-topic] -->|처리 실패| B[my-topic-retry-0]
    B -->|재시도 1 실패| C[my-topic-retry-1]
    C -->|재시도 2 실패| D[my-topic-dlt]
    D -->|수동 처리| E[관리자]
```

## 전체 예제 코드

### Producer

```java
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public MessageController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping
    public ResponseEntity<String> send(@RequestBody MessageRequest request) {
        kafkaTemplate.send(request.topic(), request.key(), request.message());
        return ResponseEntity.ok("메시지 전송 완료");
    }
}

record MessageRequest(String topic, String key, String message) {}
```

### Consumer

```java
@Component
@Slf4j
public class MessageConsumer {

    @KafkaListener(
        topics = "${app.kafka.topic}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(
            ConsumerRecord<String, String> record,
            Acknowledgment ack) {

        log.info("수신 - Partition: {}, Offset: {}, Key: {}, Value: {}",
                record.partition(),
                record.offset(),
                record.key(),
                record.value());

        try {
            processMessage(record.value());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("처리 실패: {}", record.value(), e);
            // 재시도 로직 또는 DLT 전송
        }
    }

    private void processMessage(String message) {
        // 비즈니스 로직
    }
}
```

## 테스트

### 임베디드 Kafka

```java
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"test-topic"})
class KafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void testSendAndReceive() throws Exception {
        kafkaTemplate.send("test-topic", "test-message").get();

        // Consumer 검증 로직
    }
}
```

## 정리

| 구분 | 클래스/어노테이션 | 용도 |
|------|-----------------|------|
| **Producer** | `KafkaTemplate` | 메시지 전송 |
| **Consumer** | `@KafkaListener` | 메시지 수신 |
| **수동 커밋** | `Acknowledgment` | 처리 완료 확인 |
| **에러 처리** | `@RetryableTopic` | 재시도 및 DLT |

## 다음 단계

- [주문 시스템](/docs/examples/order-system/) - 실전 예제
