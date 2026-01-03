---
title: "Consumer Group & Offset"
weight: 3
---

# Consumer Group & Offset

병렬 처리와 진행 상태 관리의 핵심 개념을 이해합니다.

## Consumer Group이란?

**Consumer Group**은 동일한 목적을 가진 Consumer들의 논리적 그룹입니다.

```mermaid
flowchart TB
    subgraph Topic["orders Topic"]
        P0[Partition 0]
        P1[Partition 1]
        P2[Partition 2]
    end

    subgraph Group["Consumer Group: order-service"]
        C1[Consumer 1]
        C2[Consumer 2]
        C3[Consumer 3]
    end

    P0 --> C1
    P1 --> C2
    P2 --> C3
```

### 핵심 규칙

> **하나의 Partition은 Consumer Group 내에서 하나의 Consumer만 읽을 수 있다**

이 규칙이 중요한 이유:
- **순서 보장**: 같은 Partition의 메시지는 순서대로 처리
- **중복 방지**: 같은 메시지를 여러 Consumer가 동시에 처리하지 않음

## Consumer 수와 Partition 수

```mermaid
flowchart TB
    subgraph Case1["Consumer < Partition"]
        P1A[P0]
        P1B[P1]
        P1C[P2]
        C1A[Consumer 1]
        C1B[Consumer 2]
        P1A --> C1A
        P1B --> C1A
        P1C --> C1B
    end

    subgraph Case2["Consumer = Partition"]
        P2A[P0]
        P2B[P1]
        P2C[P2]
        C2A[Consumer 1]
        C2B[Consumer 2]
        C2C[Consumer 3]
        P2A --> C2A
        P2B --> C2B
        P2C --> C2C
    end

    subgraph Case3["Consumer > Partition"]
        P3A[P0]
        P3B[P1]
        C3A[Consumer 1]
        C3B[Consumer 2]
        C3C["Consumer 3\n(유휴)"]
        P3A --> C3A
        P3B --> C3B
    end
```

| 상황 | 결과 |
|------|------|
| Consumer < Partition | 일부 Consumer가 여러 Partition 담당 |
| Consumer = Partition | 최적 (1:1 매핑) |
| Consumer > Partition | 일부 Consumer 유휴 상태 |

## 여러 Consumer Group

서로 다른 Consumer Group은 **독립적으로** 메시지를 소비합니다.

```mermaid
flowchart TB
    subgraph Topic["orders Topic"]
        P0[Partition 0]
        P1[Partition 1]
    end

    subgraph Group1["Group: order-service"]
        C1[Consumer]
    end

    subgraph Group2["Group: analytics-service"]
        C2[Consumer]
    end

    subgraph Group3["Group: notification-service"]
        C3[Consumer]
    end

    P0 --> C1
    P1 --> C1
    P0 --> C2
    P1 --> C2
    P0 --> C3
    P1 --> C3
```

각 그룹은:
- 모든 메시지를 독립적으로 수신
- 별도의 Offset 관리
- 서로 영향 없이 병렬 처리

## Offset이란?

**Offset**은 Partition 내 메시지의 순차적 위치 번호입니다.

```
Partition 0:
┌─────┬─────┬─────┬─────┬─────┬─────┬─────┐
│  0  │  1  │  2  │  3  │  4  │  5  │  6  │
└─────┴─────┴─────┴─────┴─────┴─────┴─────┘
                    ↑           ↑
            Current Offset    Latest Offset
              (읽은 위치)      (최신 메시지)
```

### Offset 종류

```mermaid
flowchart LR
    START[Earliest\nOffset 0]
    COMMIT[Committed\nOffset 3]
    CURRENT[Current\nOffset 5]
    END[Latest\nOffset 7]

    START --> COMMIT --> CURRENT --> END
```

| Offset 종류 | 설명 |
|------------|------|
| **Earliest** | 가장 오래된 메시지 위치 |
| **Committed** | 마지막으로 커밋된 위치 |
| **Current** | 현재 Consumer가 읽고 있는 위치 |
| **Latest** | 가장 최신 메시지 위치 |

## Offset 커밋

Consumer가 메시지를 성공적으로 처리했음을 Kafka에 알리는 과정입니다.

```mermaid
sequenceDiagram
    participant C as Consumer
    participant K as Kafka
    participant OS as Offset Storage

    C->>K: poll() - 메시지 요청
    K-->>C: Offset 3, 4, 5 메시지
    C->>C: 메시지 처리
    C->>OS: Offset 5 커밋
    OS-->>C: 커밋 완료

    Note over C,OS: 재시작 시 Offset 6부터 재개
```

### 자동 커밋 vs 수동 커밋

```yaml
# application.yml
spring:
  kafka:
    consumer:
      enable-auto-commit: true   # 자동 커밋 (기본값)
      auto-commit-interval: 5000 # 5초마다 커밋
```

| 방식 | 장점 | 단점 |
|------|------|------|
| **자동 커밋** | 구현 간단 | 처리 실패 시 데이터 유실 가능 |
| **수동 커밋** | 정확한 제어 | 구현 복잡 |

### 수동 커밋 예시

```java
@KafkaListener(topics = "orders")
public void consume(ConsumerRecord<String, String> record,
                    Acknowledgment ack) {
    try {
        processOrder(record.value());
        ack.acknowledge();  // 성공 시 커밋
    } catch (Exception e) {
        // 커밋하지 않음 - 재처리됨
        log.error("처리 실패", e);
    }
}
```

## 장애 복구 시나리오

### Consumer 장애 시

```mermaid
sequenceDiagram
    participant C1 as Consumer 1
    participant C2 as Consumer 2
    participant K as Kafka

    Note over C1,K: 정상 상태
    C1->>K: Partition 0, 1 처리 중

    Note over C1: Consumer 1 장애 발생!

    K->>K: 리밸런싱 시작
    K->>C2: Partition 0, 1 재할당

    Note over C2,K: 복구 완료
    C2->>K: Committed Offset부터 재개
```

### 리밸런싱 (Rebalancing)

Consumer Group 내 Partition 재분배 과정:

**트리거 조건:**
- Consumer 추가/제거
- Consumer 장애
- Partition 수 변경

```mermaid
flowchart LR
    subgraph Before["리밸런싱 전"]
        B_P0[P0] --> B_C1[C1]
        B_P1[P1] --> B_C1
        B_P2[P2] --> B_C2[C2]
    end

    subgraph After["C2 장애 후"]
        A_P0[P0] --> A_C1[C1]
        A_P1[P1] --> A_C1
        A_P2[P2] --> A_C1
    end

    Before -->|리밸런싱| After
```

## auto.offset.reset 설정

Consumer Group이 처음 시작하거나 Offset 정보가 없을 때의 동작:

```yaml
spring:
  kafka:
    consumer:
      auto-offset-reset: earliest  # 또는 latest
```

| 설정 | 동작 |
|------|------|
| **earliest** | 가장 오래된 메시지부터 읽기 |
| **latest** | 새로운 메시지만 읽기 |
| **none** | Offset 없으면 에러 발생 |

## 정리

```mermaid
flowchart TB
    subgraph ConsumerGroup["Consumer Group"]
        CG[같은 Group ID를\n공유하는 Consumer들]
    end

    subgraph Rules["핵심 규칙"]
        R1[1 Partition = 1 Consumer]
        R2[각 Group은 독립적]
    end

    subgraph Offset["Offset"]
        O1[메시지 위치 추적]
        O2[장애 복구 지점]
    end

    ConsumerGroup --> Rules
    ConsumerGroup --> Offset
```

| 개념 | 역할 |
|------|------|
| **Consumer Group** | 병렬 처리, 부하 분산 |
| **Offset** | 진행 상태 관리, 장애 복구 |
| **Rebalancing** | 자동 장애 복구 |

## 다음 단계

- [Replication](/docs/concepts/replication/) - 데이터 복제와 고가용성
