---
title: Aggregate 심화
weight: 3
---

# Aggregate 심화

Aggregate의 설계 원칙, 트랜잭션 경계, 실전 패턴을 깊이 있게 다룹니다.

## Aggregate란?

**Aggregate**는 데이터 변경의 단위로 취급되는 연관된 객체들의 묶음입니다.

```mermaid
flowchart TB
    subgraph OrderAggregate["Order Aggregate"]
        Order["Order<br/>(Aggregate Root)"]
        OL1["OrderLine"]
        OL2["OrderLine"]
        SA["ShippingAddress"]

        Order --> OL1
        Order --> OL2
        Order --> SA
    end

    External["외부"]
    External -->|접근| Order
    External -.->|직접 접근 불가| OL1
```

### 핵심 구성요소

| 요소 | 역할 | 예시 |
|------|------|------|
| **Aggregate Root** | 외부와의 유일한 접점, 일관성 보장 | Order |
| **내부 Entity** | Root를 통해서만 접근 | OrderLine |
| **Value Object** | 불변 속성 값 | ShippingAddress, Money |

## 설계 원칙

### 원칙 1: 진정한 불변식(Invariant)을 보호하라

**불변식**이란 항상 참이어야 하는 비즈니스 규칙입니다.

```java
public class Order {
    private List<OrderLine> orderLines;
    private Money totalAmount;
    private OrderStatus status;

    // 불변식: 주문 항목이 비어있으면 안 됨
    public void removeOrderLine(OrderLineId lineId) {
        if (orderLines.size() <= 1) {
            throw new BusinessRuleViolationException(
                "주문에는 최소 1개의 항목이 있어야 합니다"
            );
        }
        orderLines.removeIf(line -> line.getId().equals(lineId));
        recalculateTotal();  // 불변식: 총액은 항상 최신
    }

    // 불변식: 총액은 주문 항목 합계와 일치
    private void recalculateTotal() {
        this.totalAmount = orderLines.stream()
            .map(OrderLine::getAmount)
            .reduce(Money.ZERO, Money::add);
    }
}
```

### 원칙 2: 작은 Aggregate를 설계하라

```mermaid
flowchart TB
    subgraph Bad["❌ 너무 큰 Aggregate"]
        O1["Order"]
        C1["Customer"]
        P1["Product"]
        R1["Review"]

        O1 --> C1
        O1 --> P1
        P1 --> R1
    end

    subgraph Good["✅ 적절한 크기"]
        O2["Order Aggregate"]
        C2["Customer Aggregate"]
        P2["Product Aggregate"]

        O2 -.->|ID 참조| C2
        O2 -.->|ID 참조| P2
    end
```

**작게 유지해야 하는 이유:**
- 트랜잭션 범위 축소 → 동시성 충돌 감소
- 메모리 사용량 감소
- 변경 영향 범위 최소화

### 원칙 3: 다른 Aggregate는 ID로만 참조하라

```java
// ❌ 객체 직접 참조
public class Order {
    private Customer customer;  // Customer Aggregate 직접 참조
    private List<Product> products;  // Product Aggregate 직접 참조
}

// ✅ ID로 참조
public class Order {
    private CustomerId customerId;  // ID만 보관
    private List<OrderLine> orderLines;  // OrderLine 내부에 ProductId
}

public record OrderLine(
    OrderLineId id,
    ProductId productId,  // ID로 참조
    String productName,   // 필요한 정보는 복사
    Money price,
    int quantity
) {}
```

### 원칙 4: 경계 밖은 결과적 일관성(Eventual Consistency)

```mermaid
sequenceDiagram
    participant Order as Order Aggregate
    participant Event as Domain Event
    participant Stock as Stock Aggregate

    Order->>Order: confirm()
    Order->>Event: OrderConfirmedEvent
    Note right of Order: 트랜잭션 1 완료

    Event-->>Stock: 이벤트 수신
    Stock->>Stock: reserve()
    Note right of Stock: 트랜잭션 2 완료
```

```java
// Order Aggregate
public class Order {
    public void confirm() {
        this.status = OrderStatus.CONFIRMED;
        // 이벤트만 발행, 재고는 별도 트랜잭션
        registerEvent(new OrderConfirmedEvent(this.id, this.orderLines));
    }
}

// Stock Aggregate (별도 트랜잭션)
@Component
public class StockEventHandler {

    private final StockRepository stockRepository;

    // 주의: @EventListener는 같은 트랜잭션에서 동기 실행됨
    // 별도 트랜잭션이 필요하면 아래와 같이 설정
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(OrderConfirmedEvent event) {
        for (OrderLineInfo line : event.getOrderLines()) {
            Stock stock = stockRepository.findByProductId(line.getProductId());
            stock.reserve(line.getQuantity());
            stockRepository.save(stock);
        }
    }
}
```

## 트랜잭션 경계

### 하나의 트랜잭션 = 하나의 Aggregate

```java
// ✅ 올바른 패턴: 하나의 Aggregate만 수정
@Transactional
public void confirmOrder(OrderId orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));

    order.confirm();  // Order Aggregate만 수정

    orderRepository.save(order);
    // 이벤트로 다른 Aggregate 변경 유도
}

// ❌ 잘못된 패턴: 여러 Aggregate 동시 수정
@Transactional
public void confirmOrder(OrderId orderId) {
    Order order = orderRepository.findById(orderId).orElseThrow();
    order.confirm();

    // 같은 트랜잭션에서 다른 Aggregate 수정 - 피해야 함
    for (OrderLine line : order.getOrderLines()) {
        Stock stock = stockRepository.findByProductId(line.getProductId());
        stock.reserve(line.getQuantity());  // Stock Aggregate 수정
    }
}
```

### 왜 분리해야 하나?

```mermaid
flowchart TB
    subgraph Problem["문제 상황"]
        T1["트랜잭션 1:<br/>Order + Stock 동시 수정"]
        T2["트랜잭션 2:<br/>같은 Stock 수정 시도"]
        LOCK["🔒 락 경합"]
        T1 --> LOCK
        T2 --> LOCK
    end

    subgraph Solution["해결책"]
        T3["트랜잭션 1:<br/>Order만 수정"]
        T4["트랜잭션 2:<br/>Stock만 수정"]
        T3 -.->|이벤트| T4
    end
```

## Aggregate 루트 설계

### 루트를 통한 모든 변경

```java
public class Order {
    private List<OrderLine> orderLines;

    // ✅ 루트를 통해 내부 객체 추가
    public void addOrderLine(ProductId productId, String name, Money price, int qty) {
        validateCanModify();

        OrderLine newLine = new OrderLine(
            OrderLineId.generate(),
            productId,
            name,
            price,
            qty
        );
        this.orderLines.add(newLine);
        recalculateTotal();
    }

    // ✅ 루트를 통해 내부 객체 수정
    public void changeQuantity(OrderLineId lineId, int newQuantity) {
        validateCanModify();

        OrderLine line = findOrderLine(lineId);
        line.changeQuantity(newQuantity);  // 내부에서만 변경 허용
        recalculateTotal();
    }

    // 내부 객체를 직접 노출하지 않음
    public List<OrderLine> getOrderLines() {
        return Collections.unmodifiableList(orderLines);
    }
}
```

### 불변식 검증

```java
public class Order {
    private static final int MAX_ORDER_LINES = 100;
    private static final Money MAX_ORDER_AMOUNT = Money.won(10_000_000);

    public void addOrderLine(OrderLine line) {
        // 불변식 1: 주문 항목 수 제한
        if (orderLines.size() >= MAX_ORDER_LINES) {
            throw new TooManyOrderLinesException(MAX_ORDER_LINES);
        }

        orderLines.add(line);
        recalculateTotal();

        // 불변식 2: 최대 주문 금액 제한
        if (totalAmount.isGreaterThan(MAX_ORDER_AMOUNT)) {
            orderLines.remove(line);  // 롤백
            recalculateTotal();
            throw new OrderAmountExceededException(MAX_ORDER_AMOUNT);
        }
    }
}
```

## 실전 패턴

### 패턴 1: 낙관적 락(Optimistic Locking)

```java
@Entity
public class OrderEntity {
    @Id
    private String id;

    @Version  // 낙관적 락
    private Long version;

    // ...
}
```

```java
// 동시 수정 시 예외 발생
try {
    order.confirm();
    orderRepository.save(order);
} catch (OptimisticLockingFailureException e) {
    // 재시도 로직
    throw new ConcurrentModificationException("주문이 다른 곳에서 수정되었습니다");
}
```

### 패턴 2: Aggregate 복원

```java
public class Order {
    // 저장된 상태에서 복원 (Factory 패턴)
    public static Order reconstitute(
        OrderId id,
        CustomerId customerId,
        OrderStatus status,
        List<OrderLine> orderLines,
        ShippingAddress address,
        LocalDateTime createdAt
    ) {
        Order order = new Order();
        order.id = id;
        order.customerId = customerId;
        order.status = status;
        order.orderLines = new ArrayList<>(orderLines);
        order.shippingAddress = address;
        order.createdAt = createdAt;
        return order;
    }

    // 새로 생성
    public static Order create(CustomerId customerId, List<OrderLine> orderLines) {
        Order order = new Order();
        order.id = OrderId.generate();
        order.customerId = customerId;
        order.status = OrderStatus.PENDING;
        order.orderLines = new ArrayList<>(orderLines);
        order.createdAt = LocalDateTime.now();

        order.registerEvent(new OrderCreatedEvent(order.id));
        return order;
    }
}
```

### 패턴 3: 도메인 이벤트 수집

```java
public abstract class AggregateRoot {
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}

public class Order extends AggregateRoot {

    public void confirm() {
        this.status = OrderStatus.CONFIRMED;
        registerEvent(new OrderConfirmedEvent(this.id));
    }

    public void cancel(CancellationReason reason) {
        this.status = OrderStatus.CANCELLED;
        registerEvent(new OrderCancelledEvent(this.id, reason));
    }
}
```

### 패턴 4: Repository에서 이벤트 발행

```java
@Repository
public class JpaOrderRepository implements OrderRepository {

    private final OrderJpaRepository jpaRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Order save(Order order) {
        OrderEntity entity = toEntity(order);
        jpaRepository.save(entity);

        // 저장 후 이벤트 발행
        order.getDomainEvents().forEach(eventPublisher::publishEvent);
        order.clearDomainEvents();

        return order;
    }
}
```

## Aggregate 경계 결정 가이드

### 질문 체크리스트

```mermaid
flowchart TB
    Q1{함께 변경되어야<br/>하는가?}
    Q2{같은 트랜잭션이<br/>필수인가?}
    Q3{진정한 불변식이<br/>존재하는가?}

    Q1 -->|Yes| Q2
    Q1 -->|No| SEP[분리된 Aggregate]

    Q2 -->|Yes| Q3
    Q2 -->|No| SEP

    Q3 -->|Yes| SAME[같은 Aggregate]
    Q3 -->|No| SEP
```

### 예시: 주문과 결제

```
질문: 주문(Order)과 결제(Payment)는 같은 Aggregate?

1. 함께 변경되어야 하는가?
   → 주문 없이 결제는 없지만, 결제 실패해도 주문은 유지
   → No

2. 같은 트랜잭션이 필수인가?
   → 결제는 외부 PG 연동, 실패/재시도 많음
   → 분리해야 안전
   → No

3. 진정한 불변식이 있는가?
   → "주문 금액 = 결제 금액" 은 결과적 일관성으로 충분
   → No

결론: 분리된 Aggregate
```

```java
// 분리된 Aggregate
public class Order {
    private OrderId id;
    private PaymentId paymentId;  // ID로만 참조
    private PaymentStatus paymentStatus;  // 상태 복사
}

public class Payment {
    private PaymentId id;
    private OrderId orderId;  // ID로만 참조
    private Money amount;
    private PaymentStatus status;
}
```

## 안티패턴

### 1. God Aggregate

```java
// ❌ 모든 것을 포함하는 거대한 Aggregate
public class Order {
    private Customer customer;  // Customer 전체
    private List<Product> products;  // Product 전체
    private Payment payment;  // Payment 전체
    private Shipment shipment;  // Shipment 전체
    // 트랜잭션 범위가 너무 넓음
}
```

### 2. Anemic Aggregate

```java
// ❌ 로직 없는 빈약한 Aggregate
public class Order {
    private OrderId id;
    private OrderStatus status;

    // getter/setter만 존재
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}

// 로직이 서비스에 분산
public class OrderService {
    public void confirm(Order order) {
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CONFIRMED);
        }
    }
}
```

## 다음 단계

- [도메인 이벤트](../domain-events/) - 이벤트 기반 통합
