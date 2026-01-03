---
title: 아키텍처 패턴
weight: 5
---

# 아키텍처 패턴

DDD를 효과적으로 구현하기 위한 아키텍처 패턴들을 살펴봅니다.

## 왜 아키텍처가 중요한가?

```mermaid
flowchart TB
    subgraph Problem["❌ 잘못된 구조"]
        P1["UI"]
        P2["비즈니스 로직"]
        P3["데이터베이스"]
        P1 <--> P2 <--> P3
        P1 <-.->|"의존성 혼재"| P3
    end

    subgraph Solution["✅ 올바른 구조"]
        S1["UI"]
        S2["Application"]
        S3["Domain"]
        S4["Infrastructure"]
        S1 --> S2 --> S3
        S4 --> S3
    end
```

**핵심 원칙: 도메인은 어떤 것에도 의존하지 않는다**

## Layered Architecture (계층형 아키텍처)

### 기본 구조

```mermaid
flowchart TB
    subgraph Layers["4계층 아키텍처"]
        UI["Presentation Layer<br/>UI, API Controller"]
        APP["Application Layer<br/>Use Case 조율"]
        DOM["Domain Layer<br/>비즈니스 로직"]
        INFRA["Infrastructure Layer<br/>DB, 외부 시스템"]
    end

    UI --> APP
    APP --> DOM
    APP --> INFRA
    INFRA --> DOM
```

### 각 계층의 역할

| 계층 | 역할 | 포함 요소 |
|------|------|----------|
| **Presentation** | 사용자 인터페이스, API | Controller, View, DTO |
| **Application** | 유스케이스 조율, 트랜잭션 | Application Service, Command/Query |
| **Domain** | 비즈니스 로직 | Entity, Value Object, Aggregate, Domain Service |
| **Infrastructure** | 기술적 구현 | Repository 구현, 외부 API 연동 |

### 의존성 규칙

```java
// ✅ 올바른 의존 방향
presentation → application → domain ← infrastructure

// ❌ 잘못된 의존 방향
domain → infrastructure  // 도메인이 인프라에 의존하면 안 됨
```

### 한계점

```mermaid
flowchart TB
    subgraph Problem["계층형의 한계"]
        L1["모든 요청이 모든 계층을 통과"]
        L2["인프라 변경 시 도메인 영향"]
        L3["테스트 어려움"]
    end
```

## Hexagonal Architecture (헥사고날 아키텍처)

### 개념

**Ports and Adapters**라고도 불립니다. 도메인을 중심에 두고 외부와의 연결을 Port와 Adapter로 추상화합니다.

```mermaid
flowchart TB
    subgraph External["외부"]
        WEB["Web"]
        CLI["CLI"]
        MSG["Message Queue"]
        DB[(Database)]
        EXT["External API"]
    end

    subgraph Adapters["Adapters"]
        WA["Web Adapter<br/>(Controller)"]
        CA["CLI Adapter"]
        MA["Message Adapter<br/>(Listener)"]
        PA["Persistence Adapter<br/>(Repository 구현)"]
        EA["External Adapter<br/>(Client)"]
    end

    subgraph Ports["Ports"]
        IP["Inbound Ports<br/>(Use Case Interface)"]
        OP["Outbound Ports<br/>(Repository Interface)"]
    end

    subgraph Core["Application Core"]
        APP["Application Service"]
        DOM["Domain Model"]
    end

    WEB --> WA
    CLI --> CA
    MSG --> MA

    WA --> IP
    CA --> IP
    MA --> IP

    IP --> APP
    APP --> DOM
    APP --> OP

    OP --> PA
    OP --> EA

    PA --> DB
    EA --> EXT
```

### Port와 Adapter

**Port (인터페이스):**
- **Inbound Port:** 외부에서 애플리케이션으로 들어오는 요청 정의
- **Outbound Port:** 애플리케이션에서 외부로 나가는 요청 정의

**Adapter (구현체):**
- **Primary/Driving Adapter:** 애플리케이션을 호출 (Controller, CLI)
- **Secondary/Driven Adapter:** 애플리케이션이 호출 (Repository, Client)

### 구현 예시

```java
// === Inbound Port (Use Case Interface) ===
// 도메인 계층에 위치
public interface ConfirmOrderUseCase {
    void confirm(OrderId orderId);
}

// === Application Service (Use Case 구현) ===
@Service
@Transactional
public class OrderService implements ConfirmOrderUseCase {

    private final LoadOrderPort loadOrderPort;      // Outbound Port
    private final SaveOrderPort saveOrderPort;      // Outbound Port
    private final SendNotificationPort notificationPort;  // Outbound Port

    @Override
    public void confirm(OrderId orderId) {
        Order order = loadOrderPort.loadById(orderId);
        order.confirm();
        saveOrderPort.save(order);
        notificationPort.sendConfirmation(order);
    }
}

// === Outbound Ports ===
// 도메인 계층에 위치
public interface LoadOrderPort {
    Order loadById(OrderId id);
}

public interface SaveOrderPort {
    void save(Order order);
}

public interface SendNotificationPort {
    void sendConfirmation(Order order);
}

// === Primary Adapter (Controller) ===
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final ConfirmOrderUseCase confirmOrderUseCase;

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<Void> confirm(@PathVariable String orderId) {
        confirmOrderUseCase.confirm(OrderId.of(orderId));
        return ResponseEntity.ok().build();
    }
}

// === Secondary Adapter (Repository 구현) ===
@Repository
public class OrderPersistenceAdapter implements LoadOrderPort, SaveOrderPort {

    private final OrderJpaRepository jpaRepository;
    private final OrderMapper mapper;

    @Override
    public Order loadById(OrderId id) {
        return jpaRepository.findById(id.getValue())
            .map(mapper::toDomain)
            .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Override
    public void save(Order order) {
        OrderEntity entity = mapper.toEntity(order);
        jpaRepository.save(entity);
    }
}

// === Secondary Adapter (External API) ===
@Component
public class NotificationAdapter implements SendNotificationPort {

    private final NotificationClient client;

    @Override
    public void sendConfirmation(Order order) {
        client.send(new NotificationRequest(
            order.getCustomerId().getValue(),
            "주문이 확정되었습니다: " + order.getId()
        ));
    }
}
```

### 패키지 구조

```
com.example.order/
├── adapter/
│   ├── in/
│   │   └── web/
│   │       ├── OrderController.java
│   │       └── OrderRequest.java
│   └── out/
│       ├── persistence/
│       │   ├── OrderPersistenceAdapter.java
│       │   ├── OrderEntity.java
│       │   └── OrderMapper.java
│       └── notification/
│           └── NotificationAdapter.java
│
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   └── ConfirmOrderUseCase.java
│   │   └── out/
│   │       ├── LoadOrderPort.java
│   │       ├── SaveOrderPort.java
│   │       └── SendNotificationPort.java
│   └── service/
│       └── OrderService.java
│
└── domain/
    ├── Order.java
    ├── OrderLine.java
    ├── OrderId.java
    └── Money.java
```

### 장점

| 장점 | 설명 |
|------|------|
| **테스트 용이** | Port를 Mock으로 대체 가능 |
| **기술 독립** | Adapter만 변경하면 기술 교체 가능 |
| **명확한 경계** | 도메인과 외부의 경계가 명확 |
| **유연성** | 새 Adapter 추가로 다양한 인터페이스 지원 |

## Clean Architecture

### 개념

Uncle Bob(Robert C. Martin)이 제안한 아키텍처로, 의존성이 **항상 안쪽으로** 향합니다.

```mermaid
flowchart TB
    subgraph Outer["Frameworks & Drivers (Blue)"]
        subgraph Adapter["Interface Adapters (Green)"]
            subgraph UseCase["Use Cases (Red)"]
                subgraph Entity["Entities (Yellow)"]
                    E["Enterprise<br/>Business Rules"]
                end
                U["Application<br/>Business Rules"]
            end
            A["Controllers<br/>Gateways<br/>Presenters"]
        end
        F["Web<br/>DB<br/>Devices<br/>External"]
    end

    F --> A --> U --> E

    style Entity fill:#fff9c4
    style UseCase fill:#ffcdd2
    style Adapter fill:#c8e6c9
    style Outer fill:#bbdefb
```

### 의존성 규칙

```
외부 → Adapters → Use Cases → Entities

핵심: 안쪽 원은 바깥쪽 원에 대해 아무것도 모른다
```

### 각 레이어

| 레이어 | 역할 | DDD 매핑 |
|--------|------|----------|
| **Entities** | 핵심 비즈니스 규칙 | Entity, Value Object, Aggregate |
| **Use Cases** | 애플리케이션 비즈니스 규칙 | Application Service |
| **Interface Adapters** | 데이터 변환 | Controller, Repository 구현 |
| **Frameworks & Drivers** | 프레임워크, 도구 | Spring, JPA, Kafka |

### Hexagonal vs Clean

```mermaid
flowchart LR
    subgraph Hex["Hexagonal"]
        H1["Port/Adapter 중심"]
        H2["수평적 관점"]
        H3["외부 통합 강조"]
    end

    subgraph Clean["Clean"]
        C1["레이어 중심"]
        C2["동심원 관점"]
        C3["의존성 규칙 강조"]
    end

    Hex -.->|"유사"| Clean
```

**실제로는 동일한 원칙을 다른 관점에서 설명**

## Onion Architecture

### 개념

Jeffrey Palermo가 제안한 아키텍처로, Clean Architecture와 유사하지만 **도메인 모델을 더 강조**합니다.

```mermaid
flowchart TB
    subgraph Outer["Infrastructure"]
        subgraph Service["Application Services"]
            subgraph DomainService["Domain Services"]
                subgraph Model["Domain Model"]
                    M["Entities<br/>Value Objects<br/>Aggregates"]
                end
                DS["Domain Services"]
            end
            AS["Application Services"]
        end
        INF["Infrastructure<br/>UI, Tests, DB"]
    end

    INF --> AS --> DS --> M
```

### 핵심 원칙

1. **도메인 모델이 중심**
2. **의존성은 안쪽으로만**
3. **외부 관심사는 최외곽에**

## 실전: 어떤 아키텍처를 선택할까?

### 선택 가이드

```mermaid
flowchart TB
    Q1{프로젝트 규모는?}
    Q2{도메인 복잡도는?}
    Q3{팀 경험은?}

    Q1 -->|소규모| LAYER["계층형으로 시작"]
    Q1 -->|중/대규모| Q2

    Q2 -->|단순| LAYER
    Q2 -->|복잡| Q3

    Q3 -->|DDD 경험 적음| LAYER2["계층형 + 도메인 분리"]
    Q3 -->|DDD 경험 있음| HEX["Hexagonal/Clean"]
```

### 실용적 조언

| 상황 | 권장 |
|------|------|
| **스타트업, MVP** | 계층형으로 빠르게 시작, 나중에 리팩터링 |
| **복잡한 도메인** | Hexagonal/Clean으로 도메인 보호 |
| **마이크로서비스** | Hexagonal이 서비스 경계와 잘 맞음 |
| **레거시 통합** | ACL + Hexagonal로 격리 |

### 점진적 전환

```mermaid
flowchart LR
    A["1. 계층형"] --> B["2. 도메인 분리"]
    B --> C["3. Port 추출"]
    C --> D["4. Hexagonal"]

    style A fill:#ffcdd2
    style B fill:#fff9c4
    style C fill:#c8e6c9
    style D fill:#bbdefb
```

**1단계: 계층형**
```
src/
├── controller/
├── service/
├── repository/
└── entity/
```

**2단계: 도메인 분리**
```
src/
├── controller/
├── application/
├── domain/          # 분리!
│   ├── model/
│   └── repository/  # Interface
└── infrastructure/
    └── persistence/ # 구현
```

**3단계: Port 추출**
```
src/
├── adapter/in/web/
├── adapter/out/persistence/
├── application/
│   ├── port/in/
│   └── port/out/
└── domain/
```

## 공통 원칙

어떤 아키텍처를 선택하든 지켜야 할 원칙:

### 1. Dependency Inversion

```java
// ❌ 도메인이 인프라에 의존
public class Order {
    private JpaOrderRepository repository;  // JPA 의존
}

// ✅ 인프라가 도메인에 의존
// Domain
public interface OrderRepository {
    Order findById(OrderId id);
}

// Infrastructure
@Repository
public class JpaOrderRepository implements OrderRepository {
    // JPA 구현
}
```

### 2. 도메인 순수성

```java
// ❌ 도메인에 프레임워크 어노테이션
@Entity
@Table(name = "orders")
public class Order {
    @Id
    private Long id;
}

// ✅ 순수한 도메인
public class Order {
    private OrderId id;
}

// Infrastructure에서 매핑
@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    private String id;
}
```

### 3. 외부 의존성 격리

```java
// ❌ 도메인에서 외부 라이브러리 직접 사용
public class Order {
    public void sendNotification() {
        // Kafka 직접 사용
        kafkaTemplate.send("orders", this);
    }
}

// ✅ Port로 추상화
// Domain
public interface NotificationPort {
    void notify(Order order);
}

// Infrastructure
@Component
public class KafkaNotificationAdapter implements NotificationPort {
    private final KafkaTemplate kafkaTemplate;

    public void notify(Order order) {
        kafkaTemplate.send("orders", toEvent(order));
    }
}
```

## 다음 단계

- [CQRS](../cqrs/) - Command Query Responsibility Segregation
- [테스트 전략](../testing/) - 아키텍처별 테스트 방법
