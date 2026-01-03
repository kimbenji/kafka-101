---
title: 메시지 흐름
weight: 2
---

# 메시지 흐름

Kafka에서 메시지가 발행되고 소비되는 전체 과정을 이해합니다.

## 전체 흐름 개요

```mermaid
sequenceDiagram
    participant P as Producer
    participant B as Broker
    participant Part as Partition
    participant C as Consumer

    P->>B: 1. 메시지 전송
    B->>Part: 2. Partition 선택 및 저장
    B-->>P: 3. ACK 응답
    C->>B: 4. 메시지 요청 (poll)
    B->>C: 5. 메시지 전달
    C->>B: 6. Offset 커밋
```

## 1단계: 메시지 발행 (Produce)

Producer가 메시지를 Kafka에 전송합니다.

```mermaid
flowchart LR
    subgraph Producer
        MSG[메시지 생성]
        SER[직렬화]
        PART[Partition 결정]
    end

    subgraph Kafka
        B[Broker]
    end

    MSG --> SER --> PART --> B
```

### 발행 과정

1. **메시지 생성**: Key-Value 쌍으로 메시지 구성
2. **직렬화**: 객체를 바이트 배열로 변환
3. **Partition 결정**: Key 해시 또는 라운드 로빈
4. **전송**: 네트워크를 통해 Broker로 전송

```java
// Producer 코드 예시
kafkaTemplate.send("orders", orderId, orderJson);
//                  Topic    Key      Value
```

### Partition 결정 방식

```mermaid
flowchart TB
    MSG[메시지]
    KEY{Key 존재?}
    HASH[Key 해시값 % Partition 수]
    RR[라운드 로빈]
    P0[Partition 0]
    P1[Partition 1]
    P2[Partition 2]

    MSG --> KEY
    KEY -->|Yes| HASH
    KEY -->|No| RR
    HASH --> P0
    HASH --> P1
    RR --> P0
    RR --> P1
    RR --> P2
```

- **Key 있음**: 동일 Key는 항상 동일 Partition으로
- **Key 없음**: 라운드 로빈으로 균등 분배

## 2단계: 메시지 저장 (Store)

Broker가 메시지를 Partition에 저장합니다.

```mermaid
flowchart TB
    subgraph Partition["Partition 0"]
        direction LR
        O0["Offset 0\nmsg1"]
        O1["Offset 1\nmsg2"]
        O2["Offset 2\nmsg3"]
        O3["Offset 3\nmsg4"]
        NEW["Offset 4\n새 메시지"]
    end

    MSG[새 메시지] -->|추가| NEW
```

### 저장 특성

| 특성 | 설명 |
|------|------|
| **순차 저장** | 메시지는 Partition 끝에 추가 (Append-only) |
| **불변성** | 저장된 메시지는 수정 불가 |
| **Offset 할당** | 각 메시지에 고유 순번 부여 |
| **영속성** | 디스크에 저장되어 재시작해도 유지 |

### Offset이란?

```
Partition 0: [0] [1] [2] [3] [4] [5] [6] [7]
                              ↑
                         현재 Consumer 위치
```

- 각 메시지의 고유 식별자 (순차 번호)
- Consumer는 Offset을 기준으로 읽은 위치 추적
- 0부터 시작하여 무한히 증가

## 3단계: 메시지 소비 (Consume)

Consumer가 Broker로부터 메시지를 가져옵니다.

```mermaid
sequenceDiagram
    participant C as Consumer
    participant B as Broker
    participant P as Partition

    loop 폴링 루프
        C->>B: poll() - 메시지 요청
        B->>P: Offset 위치에서 읽기
        P-->>B: 메시지 반환
        B-->>C: 메시지 전달
        C->>C: 메시지 처리
        C->>B: Offset 커밋
    end
```

### 소비 과정

1. **Poll**: Consumer가 Broker에 메시지 요청
2. **Fetch**: Broker가 Partition에서 메시지 읽기
3. **처리**: Consumer가 비즈니스 로직 실행
4. **커밋**: 처리 완료된 Offset 저장

```java
// Consumer 코드 예시
@KafkaListener(topics = "orders", groupId = "order-service")
public void consume(ConsumerRecord<String, String> record) {
    String key = record.key();
    String value = record.value();
    long offset = record.offset();

    // 비즈니스 로직 처리
    processOrder(value);

    // Offset은 자동으로 커밋됨 (기본 설정)
}
```

### Pull vs Push

Kafka는 **Pull 방식**을 사용합니다:

| 방식 | 설명 | 장점 |
|------|------|------|
| **Pull** | Consumer가 필요할 때 가져감 | Consumer 처리 속도에 맞춤 |
| Push | Broker가 Consumer에게 밀어냄 | Consumer 과부하 가능 |

## 전체 흐름 예시

주문 시스템에서의 메시지 흐름:

```mermaid
sequenceDiagram
    participant User as 사용자
    participant Order as 주문 서비스
    participant Kafka as Kafka
    participant Payment as 결제 서비스
    participant Noti as 알림 서비스

    User->>Order: 주문 요청
    Order->>Kafka: 주문 이벤트 발행
    Order-->>User: 주문 접수 완료

    par 병렬 처리
        Kafka->>Payment: 주문 이벤트 전달
        Payment->>Payment: 결제 처리
    and
        Kafka->>Noti: 주문 이벤트 전달
        Noti->>Noti: 알림 발송
    end
```

## 메시지 보장 수준

```mermaid
flowchart LR
    subgraph At-Most-Once["At-Most-Once"]
        A1[발송] --> A2[커밋] --> A3[처리]
    end

    subgraph At-Least-Once["At-Least-Once"]
        B1[발송] --> B2[처리] --> B3[커밋]
    end

    subgraph Exactly-Once["Exactly-Once"]
        C1[트랜잭션 시작] --> C2[처리] --> C3[커밋]
    end
```

| 수준 | 설명 | 사용 사례 |
|------|------|----------|
| **At-Most-Once** | 최대 1번 (유실 가능) | 로그, 메트릭 |
| **At-Least-Once** | 최소 1번 (중복 가능) | 일반적인 이벤트 |
| **Exactly-Once** | 정확히 1번 | 금융 트랜잭션 |

## 다음 단계

- [Consumer Group & Offset](/kafka/concepts/consumer-group-offset/) - 병렬 처리와 상태 관리
