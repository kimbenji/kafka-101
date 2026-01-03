---
title: Producer 튜닝
weight: 7
---

# Producer 튜닝

Producer 성능을 최적화하는 핵심 설정들을 이해합니다.

## Producer 내부 구조

```mermaid
flowchart LR
    subgraph Application["애플리케이션"]
        SEND[send]
    end

    subgraph Producer["Producer 내부"]
        SER[Serializer]
        PART[Partitioner]
        BATCH[Batch\nbuffer.memory]
        SENDER[Sender Thread]
    end

    subgraph Kafka["Kafka"]
        BROKER[Broker]
    end

    SEND --> SER --> PART --> BATCH
    BATCH -->|batch.size 또는\nlinger.ms| SENDER
    SENDER --> BROKER
```

## 핵심 설정 개요

| 설정 | 기본값 | 영향 |
|------|--------|------|
| `batch.size` | 16KB | 배치 크기 |
| `linger.ms` | 0ms | 배치 대기 시간 |
| `buffer.memory` | 32MB | 전체 버퍼 크기 |
| `compression.type` | none | 압축 방식 |
| `max.in.flight.requests.per.connection` | 5 | 동시 요청 수 |

## batch.size

한 번에 전송할 메시지 배치의 최대 크기입니다.

### 동작 원리

```mermaid
flowchart TB
    subgraph SmallBatch["batch.size = 1KB"]
        S1[메시지 1개]
        S2[메시지 1개]
        S3[메시지 1개]
        SN["네트워크 요청 3번"]
    end

    subgraph LargeBatch["batch.size = 16KB"]
        L1[메시지 여러 개]
        LN["네트워크 요청 1번"]
    end
```

### 설정 가이드

```yaml
spring:
  kafka:
    producer:
      batch-size: 16384  # 16KB (기본값)
      # batch-size: 65536  # 64KB (처리량 중시)
      # batch-size: 1024   # 1KB (지연 시간 중시)
```

| 값 | 효과 | 적합한 경우 |
|----|------|------------|
| **작은 값** | 낮은 지연, 낮은 처리량 | 실시간 요구사항 |
| **큰 값** | 높은 처리량, 높은 지연 | 배치 처리 |

## linger.ms

배치가 가득 차지 않아도 전송하기까지 대기하는 시간입니다.

### 동작 원리

```mermaid
sequenceDiagram
    participant A as Application
    participant P as Producer
    participant K as Kafka

    Note over P: linger.ms = 0 (기본)
    A->>P: 메시지 1
    P->>K: 즉시 전송

    Note over P: linger.ms = 5
    A->>P: 메시지 1
    Note over P: 5ms 대기
    A->>P: 메시지 2
    A->>P: 메시지 3
    P->>K: 배치 전송 (3개)
```

### 설정 가이드

```yaml
spring:
  kafka:
    producer:
      properties:
        linger.ms: 5  # 5ms 대기
```

| 값 | 효과 | 적합한 경우 |
|----|------|------------|
| **0 (기본)** | 즉시 전송 | 지연 시간 최소화 |
| **5-10ms** | 적당한 배칭 | 일반적 권장 |
| **100ms+** | 최대 배칭 | 대용량 배치 처리 |

### batch.size + linger.ms 조합

```mermaid
flowchart TB
    MSG[메시지 도착]
    CHECK{batch.size\n도달?}
    WAIT{linger.ms\n초과?}
    SEND[배치 전송]

    MSG --> CHECK
    CHECK -->|Yes| SEND
    CHECK -->|No| WAIT
    WAIT -->|Yes| SEND
    WAIT -->|No| MSG
```

둘 중 하나의 조건이 만족되면 전송됩니다.

## buffer.memory

Producer가 사용할 수 있는 전체 버퍼 메모리입니다.

### 동작 원리

```mermaid
flowchart TB
    subgraph Buffer["buffer.memory = 32MB"]
        B1[Partition 0\n배치]
        B2[Partition 1\n배치]
        B3[Partition 2\n배치]
        FREE[여유 공간]
    end

    SEND[send] -->|버퍼에 추가| Buffer
    Buffer -->|Sender Thread| KAFKA[Kafka]
```

### 버퍼 부족 시

```mermaid
sequenceDiagram
    participant A as Application
    participant P as Producer (버퍼 가득)
    participant K as Kafka (느림)

    A->>P: send()
    Note over P: 버퍼 가득!
    Note over P: max.block.ms 동안\n대기

    alt 공간 확보됨
        K-->>P: ACK (버퍼 해제)
        P->>K: 새 메시지 전송
    else 타임아웃
        P-->>A: TimeoutException
    end
```

### 설정 가이드

```yaml
spring:
  kafka:
    producer:
      buffer-memory: 33554432  # 32MB (기본값)
      properties:
        max.block.ms: 60000  # 버퍼 대기 최대 시간
```

**권장:** `buffer.memory` > `batch.size` × Partition 수

## compression.type

메시지 압축 방식을 설정합니다.

### 압축 방식 비교

```mermaid
flowchart LR
    subgraph NoComp["압축 없음"]
        NC1["100KB"] --> NC2["100KB"]
    end

    subgraph GZIP["gzip"]
        G1["100KB"] --> G2["~30KB"]
    end

    subgraph Snappy["snappy"]
        S1["100KB"] --> S2["~50KB"]
    end

    subgraph LZ4["lz4"]
        L1["100KB"] --> L2["~45KB"]
    end

    subgraph ZSTD["zstd"]
        Z1["100KB"] --> Z2["~25KB"]
    end
```

| 방식 | 압축률 | CPU 사용 | 속도 | 권장 |
|------|--------|---------|------|------|
| **none** | 0% | 최저 | 최고 | 작은 메시지 |
| **gzip** | 최고 | 최고 | 최저 | 저장 공간 중시 |
| **snappy** | 중간 | 낮음 | 높음 | **일반 권장** |
| **lz4** | 중간 | 낮음 | 최고 | 고성능 요구 |
| **zstd** | 높음 | 중간 | 높음 | Kafka 2.1+ |

### 설정

```yaml
spring:
  kafka:
    producer:
      compression-type: snappy  # 일반 권장
      # compression-type: lz4   # 고성능
      # compression-type: zstd  # 고압축
```

### 압축의 이점

```
원본 데이터: 100MB
├── 네트워크 전송: 100MB
├── 브로커 저장: 100MB
└── 복제 전송: 200MB (RF=3)

snappy 압축: 50MB
├── 네트워크 전송: 50MB (-50%)
├── 브로커 저장: 50MB (-50%)
└── 복제 전송: 100MB (-50%)
```

## max.in.flight.requests.per.connection

하나의 연결에서 ACK 대기 중인 최대 요청 수입니다.

### 순서 보장 문제

```mermaid
sequenceDiagram
    participant P as Producer
    participant K as Kafka

    Note over P,K: max.in.flight = 5
    P->>K: 요청 1
    P->>K: 요청 2
    P->>K: 요청 3

    K--xP: 요청 1 실패
    K-->>P: 요청 2 성공
    K-->>P: 요청 3 성공

    P->>K: 요청 1 재전송
    K-->>P: 요청 1 성공

    Note over K: 순서: 2, 3, 1 (뒤섞임!)
```

### 해결책

```yaml
# 방법 1: Idempotent Producer (권장)
spring:
  kafka:
    producer:
      properties:
        enable.idempotence: true  # Kafka 3.0+ 기본값
        max.in.flight.requests.per.connection: 5  # 5까지 안전

# 방법 2: in-flight를 1로 제한 (성능 저하)
spring:
  kafka:
    producer:
      properties:
        max.in.flight.requests.per.connection: 1
```

Idempotent Producer는 시퀀스 번호로 순서를 보장합니다.

## 재시도 설정

### 핵심 설정

```yaml
spring:
  kafka:
    producer:
      retries: 2147483647  # Integer.MAX_VALUE (기본값)
      properties:
        delivery.timeout.ms: 120000  # 전체 타임아웃
        retry.backoff.ms: 100  # 재시도 간격
        request.timeout.ms: 30000  # 단일 요청 타임아웃
```

### 타임아웃 관계

```mermaid
flowchart LR
    subgraph DeliveryTimeout["delivery.timeout.ms (120초)"]
        R1[요청 1\n30초]
        W1[대기\n100ms]
        R2[재시도\n30초]
        W2[대기\n100ms]
        R3[재시도\n30초]
    end
```

**규칙:** `delivery.timeout.ms` >= `request.timeout.ms` + `linger.ms`

## 프로필별 설정 예시

### 처리량 최적화 (Throughput)

```yaml
spring:
  kafka:
    producer:
      acks: all
      batch-size: 65536  # 64KB
      compression-type: lz4
      properties:
        linger.ms: 50
        buffer.memory: 67108864  # 64MB
```

### 지연 시간 최적화 (Latency)

```yaml
spring:
  kafka:
    producer:
      acks: 1  # 또는 all
      batch-size: 1024  # 1KB
      compression-type: none
      properties:
        linger.ms: 0
```

### 균형잡힌 설정 (Balanced)

```yaml
spring:
  kafka:
    producer:
      acks: all
      batch-size: 16384  # 16KB
      compression-type: snappy
      properties:
        linger.ms: 5
        enable.idempotence: true
```

## 설정 튜닝 가이드

```mermaid
flowchart TB
    Q1{처리량 vs\n지연시간?}
    Q2{메시지 크기?}
    Q3{순서 중요?}

    Q1 -->|처리량| TH[batch.size ↑\nlinger.ms ↑\ncompression: lz4]
    Q1 -->|지연시간| LT[batch.size ↓\nlinger.ms=0]

    Q2 -->|큰 메시지| BIG[compression: zstd]
    Q2 -->|작은 메시지| SMALL[compression: none]

    Q3 -->|Yes| ORD[enable.idempotence=true]
```

## 정리

| 설정 | 처리량 ↑ | 지연시간 ↓ |
|------|----------|-----------|
| `batch.size` | ↑ 크게 | ↓ 작게 |
| `linger.ms` | ↑ 크게 | = 0 |
| `compression.type` | lz4/snappy | none |
| `buffer.memory` | ↑ 크게 | 영향 없음 |

## 다음 단계

- [Consumer 튜닝](../consumer-tuning/) - Consumer 성능 최적화
