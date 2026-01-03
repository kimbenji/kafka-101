---
title: 개념 이해
weight: 2
---

Kafka의 핵심 구성요소와 동작 원리를 이해합니다.

## 학습 순서

### 기초 개념

1. [핵심 구성요소](core-components/) - Producer, Consumer, Broker, Topic, Partition
2. [메시지 흐름](message-flow/) - 메시지가 어떻게 전달되는가
3. [Consumer Group과 Offset](consumer-group-offset/) - 병렬 처리와 진행 상태 관리
4. [Replication](replication/) - 고가용성을 위한 복제
5. [심화 개념](advanced-concepts/) - acks, Message Key, Retention, Idempotent Producer

### 심화 학습

6. [트랜잭션과 Exactly-Once](transactions/) - 메시지 전달 보장과 트랜잭션
7. [Producer 튜닝](producer-tuning/) - 배치, 압축, 성능 최적화
8. [Consumer 튜닝](consumer-tuning/) - Fetch, Poll, 커밋 전략
9. [에러 처리 심화](error-handling/) - 재시도, Dead Letter Topic
10. [모니터링 기초](monitoring/) - Lag, 메트릭, 알림 설정
