---
title: 환경 구성
weight: 1
---

# 환경 구성

Spring Boot에서 Kafka를 사용하기 위한 환경 설정 레퍼런스입니다.

> **Quick Start를 완료했다면?**
> [Quick Start](../../quick-start/)에서 이미 기본 환경을 구성했습니다. 이 문서는 설정 상세 내용과 프로덕션 환경 구성을 위한 **참조 문서**입니다.

---

## Docker로 Kafka 실행

### docker-compose.yml

프로젝트 루트의 `docker/docker-compose.yml` 파일입니다.

```yaml
version: '3.8'

services:
  kafka:
    image: apache/kafka:3.6.1
    hostname: kafka
    container_name: kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:9093
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_LOG_DIRS: /var/lib/kafka/data
      CLUSTER_ID: MkU3OEVBNTcwNTJENDM2Qk
    volumes:
      - kafka-data:/var/lib/kafka/data

volumes:
  kafka-data:
```

> **KRaft 모드**: 이 설정은 Zookeeper 없이 Kafka 자체 메타데이터 관리를 사용합니다 (Kafka 3.3+).

### 실행 명령

```bash
# 시작
docker-compose up -d

# 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs -f kafka

# 종료
docker-compose down

# 데이터 포함 종료
docker-compose down -v
```

---

## Spring Boot 의존성

### build.gradle.kts

```kotlin
plugins {
    java
    id("org.springframework.boot") version "3.2.1"
    id("io.spring.dependency-management") version "1.1.4"
}

dependencies {
    // Kafka
    implementation("org.springframework.kafka:spring-kafka")

    // Web (REST API용)
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
}
```

### Maven (pom.xml)

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```

---

## application.yml 설정

### Quick Start 기본 설정

[Quick Start 예제](../../quick-start/)에서 사용하는 최소 설정입니다.

```yaml
spring:
  application:
    name: kafka-example

  kafka:
    bootstrap-servers: localhost:9092

    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer

    consumer:
      group-id: quickstart-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
```

### 프로덕션 권장 설정

실무 환경에서는 다음 설정을 추가로 고려하세요.

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092

    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: all                    # 모든 복제본 확인
      retries: 3                   # 재시도 횟수
      properties:
        linger.ms: 1               # 배치 대기 시간
        enable.idempotence: true   # 중복 전송 방지

    consumer:
      group-id: quickstart-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      enable-auto-commit: true
      properties:
        max.poll.records: 500
        max.poll.interval.ms: 300000
```

---

## 설정 항목 상세

### Producer 설정

| 설정 | 설명 | 기본값 | 권장값 |
|------|------|--------|--------|
| `acks` | 전송 확인 수준 | `1` | `all` (프로덕션) |
| `retries` | 재시도 횟수 | `2147483647` | `3` |
| `batch-size` | 배치 크기 (bytes) | `16384` | `16384` |
| `linger-ms` | 배치 대기 시간 | `0` | `1` |
| `buffer-memory` | 버퍼 메모리 | `33554432` | `33554432` |

### Consumer 설정

| 설정 | 설명 | 기본값 | 권장값 |
|------|------|--------|--------|
| `group-id` | Consumer Group ID | - | 서비스명 |
| `auto-offset-reset` | 초기 Offset | `latest` | `earliest` (개발) |
| `enable-auto-commit` | 자동 커밋 | `true` | 상황에 따라 |
| `max-poll-records` | 한번에 가져올 최대 레코드 | `500` | `500` |

---

## JSON 메시지 처리

### 의존성 추가

```kotlin
dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind")
}
```

### 설정

```yaml
spring:
  kafka:
    producer:
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "com.example.*"
```

### 사용 예시

```java
// 도메인 클래스
public record OrderEvent(
    String orderId,
    String status,
    LocalDateTime timestamp
) {}

// Producer
kafkaTemplate.send("orders", orderId, new OrderEvent(orderId, "CREATED", now()));

// Consumer
@KafkaListener(topics = "orders")
public void consume(OrderEvent event) {
    log.info("주문 이벤트: {}", event);
}
```

---

## 프로필별 설정

### application.yml (공통)

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_SERVERS:localhost:9092}
```

### application-local.yml

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      auto-offset-reset: earliest
```

### application-prod.yml

```yaml
spring:
  kafka:
    bootstrap-servers: kafka-1:9092,kafka-2:9092,kafka-3:9092
    producer:
      acks: all
    consumer:
      auto-offset-reset: latest
```

---

## 일반적인 오류와 해결

### 연결 오류

```
Connection to node -1 could not be established
```

**원인:** Kafka 브로커에 연결할 수 없음

**해결:**
1. Kafka 실행 확인: `docker-compose ps`
2. 포트 확인: `netstat -an | grep 9092`
3. bootstrap-servers 설정 확인

### Serialization 오류

```
Failed to serialize value
```

**원인:** Serializer 설정 불일치

**해결:**
```yaml
spring:
  kafka:
    producer:
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

### Deserialization 오류

```
Failed to deserialize; nested exception is java.lang.IllegalArgumentException
```

**원인:** 신뢰할 수 있는 패키지 미설정

**해결:**
```yaml
spring:
  kafka:
    consumer:
      properties:
        spring.json.trusted.packages: "*"  # 또는 특정 패키지
```

### Group ID 누락

```
group.id is required
```

**원인:** Consumer group-id 미설정

**해결:**
```yaml
spring:
  kafka:
    consumer:
      group-id: quickstart-group
```

---

## 설정 확인 체크리스트

- [ ] Docker로 Kafka 실행됨
- [ ] spring-kafka 의존성 추가됨
- [ ] bootstrap-servers 설정됨
- [ ] Producer serializer 설정됨
- [ ] Consumer deserializer 설정됨
- [ ] Consumer group-id 설정됨
- [ ] (JSON 사용 시) trusted.packages 설정됨

---

## 다음 단계

- [기본 예제](../basic/) - Producer/Consumer 구현
- [주문 시스템](../order-system/) - 실전 예제
