---
title: "Replication"
weight: 4
---

# Replication

데이터 복제와 고가용성 메커니즘을 이해합니다.

## 왜 Replication이 필요한가?

단일 Broker에 데이터를 저장하면 장애 시 데이터 유실이 발생합니다.

```mermaid
flowchart TB
    subgraph Problem["복제 없는 경우"]
        P1[Producer] --> B1[Broker 1]
        B1 -->|장애!| X[데이터 유실]
    end

    subgraph Solution["복제 있는 경우"]
        P2[Producer] --> B2[Broker 1\nLeader]
        B2 -->|복제| B3[Broker 2\nFollower]
        B2 -->|복제| B4[Broker 3\nFollower]
        B2 -->|장애| B3
        B3 -->|승격| B3L[새 Leader]
    end
```

## Leader와 Follower

각 Partition은 하나의 **Leader**와 여러 **Follower**로 구성됩니다.

```mermaid
flowchart TB
    subgraph Partition["Topic A - Partition 0"]
        L[Broker 1\nLeader]
        F1[Broker 2\nFollower]
        F2[Broker 3\nFollower]
    end

    P[Producer] -->|쓰기| L
    L -->|복제| F1
    L -->|복제| F2
    L -->|읽기| C[Consumer]
```

### 역할 분담

| 역할 | 책임 |
|------|------|
| **Leader** | 모든 읽기/쓰기 처리, Follower에 데이터 복제 |
| **Follower** | Leader 데이터 복제, Leader 장애 시 승격 대기 |

> **중요:** Producer와 Consumer는 **Leader에만** 연결됩니다.

## Replication Factor

**Replication Factor**는 각 Partition의 복제본 수입니다.

```mermaid
flowchart LR
    subgraph RF1["RF=1 (복제 없음)"]
        RF1_B1[Broker 1\nPartition 0]
    end

    subgraph RF2["RF=2"]
        RF2_B1[Broker 1\nLeader]
        RF2_B2[Broker 2\nFollower]
    end

    subgraph RF3["RF=3 (권장)"]
        RF3_B1[Broker 1\nLeader]
        RF3_B2[Broker 2\nFollower]
        RF3_B3[Broker 3\nFollower]
    end
```

### RF별 특성

| RF | 내결함성 | 저장 비용 | 권장 사용 |
|----|---------|----------|----------|
| 1 | 없음 | 1x | 개발/테스트 |
| 2 | 1 Broker 장애 허용 | 2x | 일반 |
| 3 | 2 Broker 장애 허용 | 3x | **프로덕션 권장** |

## ISR (In-Sync Replicas)

**ISR**은 Leader와 동기화된 복제본 집합입니다.

```mermaid
flowchart TB
    subgraph Healthy["정상 상태"]
        H_L[Leader\nOffset: 100]
        H_F1[Follower 1\nOffset: 100]
        H_F2[Follower 2\nOffset: 100]
        H_ISR["ISR: {Leader, F1, F2}"]
    end

    subgraph Lagging["동기화 지연"]
        L_L[Leader\nOffset: 100]
        L_F1[Follower 1\nOffset: 100]
        L_F2[Follower 2\nOffset: 80]
        L_ISR["ISR: {Leader, F1}"]
        L_NOTE[F2는 ISR에서 제외]
    end
```

### ISR 조건

Follower가 ISR에 포함되려면:
- `replica.lag.time.max.ms` 이내에 Leader와 동기화
- 기본값: 30초

```mermaid
sequenceDiagram
    participant L as Leader
    participant F1 as Follower 1
    participant F2 as Follower 2

    L->>L: 메시지 수신 (Offset 100)
    L->>F1: 복제
    L->>F2: 복제

    F1-->>L: 동기화 완료
    Note over L,F1: ISR 유지

    Note over F2: 네트워크 지연
    Note over L,F2: 30초 초과 시 ISR 제외
```

## Leader Election

Leader 장애 시 새로운 Leader를 선출하는 과정입니다.

```mermaid
sequenceDiagram
    participant C as Controller
    participant L as Leader (Broker 1)
    participant F1 as Follower 1 (Broker 2)
    participant F2 as Follower 2 (Broker 3)

    Note over L: Leader 장애 발생!

    C->>C: 장애 감지
    C->>C: ISR 중 새 Leader 선택
    C->>F1: Leader 승격 통보
    F1->>F1: Leader로 승격

    Note over F1: 새 Leader
    C->>F2: 새 Leader 정보 전파
```

### 선출 규칙

1. **ISR 우선**: ISR 내의 Follower 중에서 선출
2. **Unclean Leader Election**: ISR이 비어있을 때 비동기 Follower 선출 (데이터 유실 가능)

```yaml
# Topic 설정
unclean.leader.election.enable: false  # 권장: 데이터 유실 방지
```

## min.insync.replicas

메시지 쓰기 시 필요한 최소 ISR 수입니다.

```mermaid
flowchart TB
    subgraph Config["RF=3, min.insync.replicas=2"]
        L[Leader]
        F1[Follower 1]
        F2[Follower 2]
    end

    subgraph Scenario1["정상: ISR=3"]
        S1[쓰기 성공]
    end

    subgraph Scenario2["F2 장애: ISR=2"]
        S2[쓰기 성공]
    end

    subgraph Scenario3["F1,F2 장애: ISR=1"]
        S3[쓰기 실패!]
    end
```

### 권장 설정

| 환경 | RF | min.insync.replicas |
|------|----|--------------------|
| 개발 | 1 | 1 |
| 프로덕션 | 3 | 2 |

## Zookeeper vs KRaft

Kafka의 메타데이터 관리 방식 비교:

```mermaid
flowchart TB
    subgraph Zookeeper["Zookeeper 모드 (구버전)"]
        ZK[Zookeeper Cluster]
        KB1[Kafka Broker 1]
        KB2[Kafka Broker 2]
        KB3[Kafka Broker 3]
        ZK <--> KB1
        ZK <--> KB2
        ZK <--> KB3
    end

    subgraph KRaft["KRaft 모드 (신버전)"]
        KR1[Kafka Broker 1\nController]
        KR2[Kafka Broker 2]
        KR3[Kafka Broker 3]
        KR1 <--> KR2
        KR1 <--> KR3
    end
```

### 비교

| 항목 | Zookeeper | KRaft |
|------|-----------|-------|
| **외부 의존성** | 필요 | 불필요 |
| **운영 복잡도** | 높음 | 낮음 |
| **Partition 확장성** | 제한적 | 향상 |
| **복구 시간** | 느림 | 빠름 |
| **Kafka 버전** | 2.x 이하 | 3.3+ 권장 |

> **권장:** 신규 프로젝트는 **KRaft 모드**를 사용하세요.

### KRaft 설정 예시

```yaml
# docker-compose.yml
environment:
  KAFKA_PROCESS_ROLES: broker,controller
  KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:9093
```

## 정리

```mermaid
flowchart TB
    subgraph Replication["Replication 핵심"]
        R1[Leader/Follower 구조]
        R2[ISR - 동기화된 복제본]
        R3[자동 Leader Election]
    end

    subgraph Config["권장 설정"]
        C1["RF=3"]
        C2["min.insync.replicas=2"]
        C3["KRaft 모드"]
    end

    Replication --> Config
```

| 개념 | 역할 |
|------|------|
| **Replication Factor** | 데이터 복사본 수 |
| **ISR** | 동기화된 복제본 집합 |
| **Leader Election** | 자동 장애 복구 |
| **KRaft** | 단순화된 클러스터 관리 |

## 다음 단계

- [심화 개념](/docs/concepts/advanced-concepts/) - acks, Message Key, Retention
