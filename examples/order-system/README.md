# 주문 시스템 예제

Kafka를 활용한 이벤트 기반 주문 시스템 예제입니다.

## 아키텍처

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  REST API       │────▶│  Kafka          │────▶│  Consumer       │
│  (OrderController)    │  (order-events) │     │  (OrderConsumer)│
└─────────────────┘     └─────────────────┘     └─────────────────┘
```

## 이벤트 타입

| 상태 | 설명 |
|------|------|
| CREATED | 주문 생성 |
| PAID | 결제 완료 |
| SHIPPED | 배송 시작 |
| DELIVERED | 배송 완료 |
| CANCELLED | 주문 취소 |

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
cd examples/order-system
./gradlew bootRun
```

### 3. API 테스트

#### 주문 생성

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId": "customer-123"}'
```

응답:
```json
{"orderId": "abc12345", "message": "주문이 생성되었습니다"}
```

#### 결제 완료

```bash
curl -X POST "http://localhost:8080/api/orders/abc12345/pay?customerId=customer-123"
```

#### 배송 시작

```bash
curl -X POST "http://localhost:8080/api/orders/abc12345/ship?customerId=customer-123"
```

#### 배송 완료

```bash
curl -X POST "http://localhost:8080/api/orders/abc12345/deliver?customerId=customer-123"
```

#### 주문 취소

```bash
curl -X POST "http://localhost:8080/api/orders/abc12345/cancel?customerId=customer-123&reason=고객요청"
```

### 4. 로그 확인

Consumer가 이벤트를 처리하면 다음과 같은 로그가 출력됩니다:

```
========================================
이벤트 수신
  Partition: 0, Offset: 5
  Key (OrderId): abc12345
  CustomerId: customer-123
  Status: CREATED
  Description: 주문이 생성되었습니다
  Timestamp: 2024-12-25T10:30:00
========================================
[처리] 주문 생성 - 재고 확인 및 결제 대기
```

## 순서 보장

동일한 `orderId`를 Key로 사용하여 같은 주문의 이벤트가 순서대로 처리됩니다.

```
Order A: CREATED → PAID → SHIPPED → DELIVERED
         (모두 같은 Partition, 순서 보장)
```

## 프로젝트 구조

```
order-system/
├── src/main/java/com/example/order/
│   ├── OrderSystemApplication.java    # 메인 클래스
│   ├── controller/
│   │   └── OrderController.java       # REST API
│   ├── domain/
│   │   ├── OrderEvent.java           # 이벤트 모델
│   │   └── OrderStatus.java          # 상태 Enum
│   ├── producer/
│   │   └── OrderProducer.java        # Kafka Producer
│   └── consumer/
│       └── OrderConsumer.java        # Kafka Consumer
└── src/main/resources/
    └── application.yml               # 설정
```
