---
title: 주문 도메인
weight: 2
---

# 주문 도메인 구현

DDD 패턴을 적용하여 주문 도메인을 구현합니다.

## 도메인 모델 설계

### Order Aggregate 구조

```mermaid
flowchart TB
    subgraph OrderAggregate["Order Aggregate"]
        Order["Order<br/>(Aggregate Root)"]
        OL1["OrderLine"]
        OL2["OrderLine"]
        ADDR["ShippingAddress<br/>(Value Object)"]
        MONEY["Money<br/>(Value Object)"]
    end

    Order --> OL1
    Order --> OL2
    Order --> ADDR
    Order --> MONEY

    CID["CustomerId"] -.->|ID 참조| Order
    PID["ProductId"] -.->|ID 참조| OL1
    PID2["ProductId"] -.->|ID 참조| OL2
```

## Value Object 구현

### OrderId

```java
package com.example.order.domain.model;

import java.util.Objects;
import java.util.UUID;

public record OrderId(String value) {

    public OrderId {
        Objects.requireNonNull(value, "OrderId는 null일 수 없습니다");
        if (value.isBlank()) {
            throw new IllegalArgumentException("OrderId는 빈 값일 수 없습니다");
        }
    }

    public static OrderId generate() {
        return new OrderId("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }

    public static OrderId of(String value) {
        return new OrderId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
```

### Money

```java
package com.example.order.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) {

    public static final Money ZERO = Money.won(0);

    public Money {
        Objects.requireNonNull(amount, "금액은 필수입니다");
        Objects.requireNonNull(currency, "통화는 필수입니다");
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("금액은 0 이상이어야 합니다: " + amount);
        }
        amount = amount.setScale(0, RoundingMode.HALF_UP);
    }

    // 팩토리 메서드
    public static Money won(long amount) {
        return new Money(BigDecimal.valueOf(amount), Currency.getInstance("KRW"));
    }

    public static Money won(BigDecimal amount) {
        return new Money(amount, Currency.getInstance("KRW"));
    }

    // 연산
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        validateSameCurrency(other);
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("결과 금액이 음수가 됩니다");
        }
        return new Money(result, this.currency);
    }

    public Money multiply(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("수량은 0 이상이어야 합니다");
        }
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)), this.currency);
    }

    public Money multiply(BigDecimal rate) {
        return new Money(this.amount.multiply(rate), this.currency);
    }

    // 비교
    public boolean isGreaterThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isGreaterThanOrEqual(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) >= 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    private void validateSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                String.format("통화가 다릅니다: %s vs %s", this.currency, other.currency)
            );
        }
    }

    @Override
    public String toString() {
        return String.format("%s %s", currency.getSymbol(), amount.toPlainString());
    }
}
```

### ShippingAddress

```java
package com.example.order.domain.model;

import java.util.Objects;

public record ShippingAddress(
    String zipCode,
    String city,
    String street,
    String detail,
    String receiverName,
    String receiverPhone
) {

    public ShippingAddress {
        Objects.requireNonNull(zipCode, "우편번호는 필수입니다");
        Objects.requireNonNull(city, "도시는 필수입니다");
        Objects.requireNonNull(street, "도로명은 필수입니다");
        Objects.requireNonNull(receiverName, "수령인 이름은 필수입니다");
        Objects.requireNonNull(receiverPhone, "수령인 전화번호는 필수입니다");

        if (!zipCode.matches("\\d{5}")) {
            throw new IllegalArgumentException("우편번호는 5자리 숫자여야 합니다: " + zipCode);
        }

        if (!receiverPhone.matches("\\d{2,3}-\\d{3,4}-\\d{4}")) {
            throw new IllegalArgumentException("전화번호 형식이 올바르지 않습니다: " + receiverPhone);
        }
    }

    public String fullAddress() {
        String base = String.format("(%s) %s %s", zipCode, city, street);
        return detail != null && !detail.isBlank()
            ? base + " " + detail
            : base;
    }

    public String receiverInfo() {
        return String.format("%s (%s)", receiverName, receiverPhone);
    }
}
```

## Entity 구현

### OrderLine (내부 Entity)

```java
package com.example.order.domain.model;

import java.util.Objects;
import java.util.UUID;

public class OrderLine extends Entity<OrderLineId> {

    private final OrderLineId id;
    private final ProductId productId;
    private final String productName;
    private final Money price;
    private int quantity;

    // 생성자 (패키지 프라이빗 - Order를 통해서만 생성)
    OrderLine(ProductId productId, String productName, Money price, int quantity) {
        validateQuantity(quantity);
        Objects.requireNonNull(productId, "상품 ID는 필수입니다");
        Objects.requireNonNull(productName, "상품명은 필수입니다");
        Objects.requireNonNull(price, "가격은 필수입니다");

        this.id = OrderLineId.generate();
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
    }

    @Override
    public OrderLineId getId() {
        return id;
    }

    public ProductId getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Money getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    // 주문 항목 금액 계산
    public Money getAmount() {
        return price.multiply(quantity);
    }

    // 수량 변경 (Order를 통해서만 호출)
    void changeQuantity(int newQuantity) {
        validateQuantity(newQuantity);
        this.quantity = newQuantity;
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다: " + quantity);
        }
        if (quantity > 999) {
            throw new IllegalArgumentException("수량은 999 이하여야 합니다: " + quantity);
        }
    }

    // DB에서 복원 (패키지 프라이빗 - Mapper에서만 사용)
    static OrderLine reconstitute(
        OrderLineId id,
        ProductId productId,
        String productName,
        Money price,
        int quantity
    ) {
        // 복원 전용 private 생성자 호출
        return new OrderLine(id, productId, productName, price, quantity);
    }

    // 복원 전용 private 생성자
    private OrderLine(OrderLineId id, ProductId productId, String productName, Money price, int quantity) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
    }
}

// OrderLineId
public record OrderLineId(String value) {
    public static OrderLineId generate() {
        return new OrderLineId(UUID.randomUUID().toString());
    }

    public static OrderLineId of(String value) {
        return new OrderLineId(value);
    }
}
```

## Aggregate Root 구현

### Order

```java
package com.example.order.domain.model;

import com.example.order.domain.event.OrderCancelledEvent;
import com.example.order.domain.event.OrderConfirmedEvent;
import com.example.order.domain.event.OrderCreatedEvent;
import com.example.order.domain.exception.IllegalOrderStateException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Order extends AggregateRoot<OrderId> {

    private static final int MAX_ORDER_LINES = 100;
    private static final Money MAX_ORDER_AMOUNT = Money.won(100_000_000);

    private final OrderId id;
    private final CustomerId customerId;
    private final List<OrderLine> orderLines;
    private ShippingAddress shippingAddress;
    private OrderStatus status;
    private Money totalAmount;
    private final LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;

    // private 생성자
    private Order(OrderId id, CustomerId customerId, ShippingAddress shippingAddress) {
        this.id = id;
        this.customerId = customerId;
        this.orderLines = new ArrayList<>();
        this.shippingAddress = shippingAddress;
        this.status = OrderStatus.PENDING;
        this.totalAmount = Money.ZERO;
        this.createdAt = LocalDateTime.now();
    }

    // 팩토리 메서드: 새 주문 생성
    public static Order create(
        CustomerId customerId,
        ShippingAddress shippingAddress,
        List<OrderLineRequest> lineRequests
    ) {
        Objects.requireNonNull(customerId, "고객 ID는 필수입니다");
        Objects.requireNonNull(shippingAddress, "배송지는 필수입니다");

        if (lineRequests == null || lineRequests.isEmpty()) {
            throw new IllegalArgumentException("주문 항목은 최소 1개 이상이어야 합니다");
        }

        Order order = new Order(OrderId.generate(), customerId, shippingAddress);

        for (OrderLineRequest request : lineRequests) {
            order.addOrderLineInternal(
                request.productId(),
                request.productName(),
                request.price(),
                request.quantity()
            );
        }

        order.registerEvent(new OrderCreatedEvent(order));
        return order;
    }

    // 팩토리 메서드: DB에서 복원
    public static Order reconstitute(
        OrderId id,
        CustomerId customerId,
        List<OrderLine> orderLines,
        ShippingAddress shippingAddress,
        OrderStatus status,
        Money totalAmount,
        LocalDateTime createdAt,
        LocalDateTime confirmedAt,
        LocalDateTime cancelledAt,
        String cancellationReason
    ) {
        Order order = new Order(id, customerId, shippingAddress);
        order.orderLines.addAll(orderLines);
        order.status = status;
        order.totalAmount = totalAmount;
        order.confirmedAt = confirmedAt;
        order.cancelledAt = cancelledAt;
        order.cancellationReason = cancellationReason;
        // 복원 시에는 이벤트 발행하지 않음
        return order;
    }

    // === 비즈니스 메서드 ===

    // 주문 확정
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalOrderStateException(
                String.format("PENDING 상태의 주문만 확정할 수 있습니다. 현재 상태: %s", this.status)
            );
        }

        this.status = OrderStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();

        registerEvent(new OrderConfirmedEvent(this));
    }

    // 주문 취소
    public void cancel(String reason) {
        if (!isCancellable()) {
            throw new IllegalOrderStateException(
                String.format("취소할 수 없는 상태입니다. 현재 상태: %s", this.status)
            );
        }

        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;

        registerEvent(new OrderCancelledEvent(this.id, reason));
    }

    // 배송지 변경
    public void changeShippingAddress(ShippingAddress newAddress) {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalOrderStateException("PENDING 상태에서만 배송지를 변경할 수 있습니다");
        }

        Objects.requireNonNull(newAddress, "새 배송지는 필수입니다");
        this.shippingAddress = newAddress;
    }

    // 주문 항목 수량 변경
    public void changeOrderLineQuantity(OrderLineId lineId, int newQuantity) {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalOrderStateException("PENDING 상태에서만 수량을 변경할 수 있습니다");
        }

        OrderLine line = findOrderLine(lineId);
        line.changeQuantity(newQuantity);
        recalculateTotal();
    }

    // 주문 항목 제거
    public void removeOrderLine(OrderLineId lineId) {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalOrderStateException("PENDING 상태에서만 항목을 제거할 수 있습니다");
        }

        if (orderLines.size() <= 1) {
            throw new IllegalArgumentException("주문에는 최소 1개의 항목이 있어야 합니다");
        }

        orderLines.removeIf(line -> line.getId().equals(lineId));
        recalculateTotal();
    }

    // === 내부 메서드 ===

    private void addOrderLineInternal(ProductId productId, String name, Money price, int qty) {
        if (orderLines.size() >= MAX_ORDER_LINES) {
            throw new IllegalArgumentException(
                String.format("주문 항목은 최대 %d개까지 가능합니다", MAX_ORDER_LINES)
            );
        }

        OrderLine newLine = new OrderLine(productId, name, price, qty);
        orderLines.add(newLine);
        recalculateTotal();

        // 최대 금액 검증
        if (totalAmount.isGreaterThan(MAX_ORDER_AMOUNT)) {
            orderLines.remove(newLine);
            recalculateTotal();
            throw new IllegalArgumentException(
                String.format("주문 총액이 최대 금액(%s)을 초과합니다", MAX_ORDER_AMOUNT)
            );
        }
    }

    private void recalculateTotal() {
        this.totalAmount = orderLines.stream()
            .map(OrderLine::getAmount)
            .reduce(Money.ZERO, Money::add);
    }

    private OrderLine findOrderLine(OrderLineId lineId) {
        return orderLines.stream()
            .filter(line -> line.getId().equals(lineId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "주문 항목을 찾을 수 없습니다: " + lineId
            ));
    }

    private boolean isCancellable() {
        return this.status == OrderStatus.PENDING
            || this.status == OrderStatus.CONFIRMED;
    }

    // === Getter ===

    @Override
    public OrderId getId() {
        return id;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public List<OrderLine> getOrderLines() {
        return Collections.unmodifiableList(orderLines);
    }

    public ShippingAddress getShippingAddress() {
        return shippingAddress;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }
}

// 주문 생성 요청 DTO
public record OrderLineRequest(
    ProductId productId,
    String productName,
    Money price,
    int quantity
) {}
```

### OrderStatus

```java
package com.example.order.domain.model;

public enum OrderStatus {
    PENDING("대기중"),
    CONFIRMED("확정됨"),
    SHIPPED("배송중"),
    DELIVERED("배송완료"),
    CANCELLED("취소됨");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
```

## 도메인 이벤트 구현

### OrderCreatedEvent

```java
package com.example.order.domain.event;

import com.example.order.domain.model.*;

import java.util.List;

public class OrderCreatedEvent extends DomainEvent {

    private final OrderId orderId;
    private final CustomerId customerId;
    private final Money totalAmount;
    private final List<OrderLineSnapshot> orderLines;
    private final ShippingAddress shippingAddress;

    public OrderCreatedEvent(Order order) {
        super();
        this.orderId = order.getId();
        this.customerId = order.getCustomerId();
        this.totalAmount = order.getTotalAmount();
        this.orderLines = order.getOrderLines().stream()
            .map(OrderLineSnapshot::from)
            .toList();
        this.shippingAddress = order.getShippingAddress();
    }

    @Override
    public String getAggregateId() {
        return orderId.value();
    }

    // Getters...

    public record OrderLineSnapshot(
        ProductId productId,
        String productName,
        int quantity,
        Money amount
    ) {
        public static OrderLineSnapshot from(OrderLine line) {
            return new OrderLineSnapshot(
                line.getProductId(),
                line.getProductName(),
                line.getQuantity(),
                line.getAmount()
            );
        }
    }
}
```

### OrderConfirmedEvent

```java
package com.example.order.domain.event;

import com.example.order.domain.model.*;

import java.time.LocalDateTime;
import java.util.List;

public class OrderConfirmedEvent extends DomainEvent {

    private final OrderId orderId;
    private final CustomerId customerId;
    private final Money totalAmount;
    private final List<OrderLineSnapshot> orderLines;
    private final LocalDateTime confirmedAt;

    public OrderConfirmedEvent(Order order) {
        super();
        this.orderId = order.getId();
        this.customerId = order.getCustomerId();
        this.totalAmount = order.getTotalAmount();
        this.orderLines = order.getOrderLines().stream()
            .map(line -> new OrderLineSnapshot(
                line.getProductId(),
                line.getQuantity()
            ))
            .toList();
        this.confirmedAt = order.getConfirmedAt();
    }

    @Override
    public String getAggregateId() {
        return orderId.value();
    }

    // Getters...

    public record OrderLineSnapshot(
        ProductId productId,
        int quantity
    ) {}
}
```

## Repository 인터페이스

```java
package com.example.order.domain.repository;

import com.example.order.domain.model.*;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(OrderId id);

    List<Order> findByCustomerId(CustomerId customerId);

    List<Order> findByStatus(OrderStatus status);

    boolean existsById(OrderId id);
}
```

## 단위 테스트

```java
package com.example.order.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class OrderTest {

    @Nested
    @DisplayName("주문 생성")
    class CreateOrder {

        @Test
        @DisplayName("유효한 정보로 주문을 생성할 수 있다")
        void createWithValidData() {
            // given
            var customerId = CustomerId.of("CUST-001");
            var address = new ShippingAddress(
                "12345", "서울시", "강남대로 123", "101호",
                "홍길동", "010-1234-5678"
            );
            var lineRequests = List.of(
                new OrderLineRequest(
                    ProductId.of("PROD-001"),
                    "상품1",
                    Money.won(10000),
                    2
                )
            );

            // when
            Order order = Order.create(customerId, address, lineRequests);

            // then
            assertThat(order.getId()).isNotNull();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getTotalAmount()).isEqualTo(Money.won(20000));
            assertThat(order.getOrderLines()).hasSize(1);
            assertThat(order.getDomainEvents()).hasSize(1);
            assertThat(order.getDomainEvents().get(0)).isInstanceOf(OrderCreatedEvent.class);
        }

        @Test
        @DisplayName("주문 항목 없이 생성하면 예외가 발생한다")
        void failWithEmptyOrderLines() {
            // given
            var customerId = CustomerId.of("CUST-001");
            var address = createValidAddress();

            // when & then
            assertThatThrownBy(() -> Order.create(customerId, address, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("최소 1개");
        }
    }

    @Nested
    @DisplayName("주문 확정")
    class ConfirmOrder {

        @Test
        @DisplayName("PENDING 상태의 주문을 확정할 수 있다")
        void confirmPendingOrder() {
            // given
            Order order = createPendingOrder();

            // when
            order.confirm();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
            assertThat(order.getConfirmedAt()).isNotNull();
        }

        @Test
        @DisplayName("이미 확정된 주문은 다시 확정할 수 없다")
        void cannotConfirmAlreadyConfirmed() {
            // given
            Order order = createPendingOrder();
            order.confirm();

            // when & then
            assertThatThrownBy(order::confirm)
                .isInstanceOf(IllegalOrderStateException.class)
                .hasMessageContaining("PENDING");
        }
    }

    @Nested
    @DisplayName("주문 취소")
    class CancelOrder {

        @Test
        @DisplayName("PENDING 상태의 주문을 취소할 수 있다")
        void cancelPendingOrder() {
            // given
            Order order = createPendingOrder();

            // when
            order.cancel("단순 변심");

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(order.getCancellationReason()).isEqualTo("단순 변심");
        }

        @Test
        @DisplayName("CONFIRMED 상태의 주문을 취소할 수 있다")
        void cancelConfirmedOrder() {
            // given
            Order order = createPendingOrder();
            order.confirm();

            // when
            order.cancel("재고 부족");

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }
    }

    // Helper methods
    private Order createPendingOrder() {
        return Order.create(
            CustomerId.of("CUST-001"),
            createValidAddress(),
            List.of(new OrderLineRequest(
                ProductId.of("PROD-001"),
                "상품1",
                Money.won(10000),
                1
            ))
        );
    }

    private ShippingAddress createValidAddress() {
        return new ShippingAddress(
            "12345", "서울시", "강남대로 123", "101호",
            "홍길동", "010-1234-5678"
        );
    }
}
```

## 다음 단계

- [애플리케이션 계층](../application-layer/) - Use Case와 서비스 구현
