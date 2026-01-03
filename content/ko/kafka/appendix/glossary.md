---
title: 용어 사전
weight: 1
---

# Kafka 용어 사전

Kafka 관련 주요 용어를 정리합니다.

## A

### ACK (Acknowledgment)
Producer가 메시지 전송 성공을 확인받는 방식. `acks=0`, `acks=1`, `acks=all` 옵션이 있음.
→ [심화 개념](../../concepts/advanced-concepts/#acks-acknowledgment)

### Auto Offset Reset
Consumer Group이 처음 시작하거나 Offset 정보가 없을 때 읽기 시작할 위치. `earliest` 또는 `latest`.
→ [Consumer Group & Offset](../../concepts/consumer-group-offset/#autooffsetreset-설정)

## B

### Broker
Kafka 서버. 메시지를 저장하고 Consumer에게 전달하는 역할.
→ [핵심 구성요소](../../concepts/core-components/#3-broker-브로커)

### Bootstrap Servers
Kafka 클러스터에 처음 연결할 때 사용하는 Broker 주소 목록. `localhost:9092` 형태.

## C

### Commit (Offset Commit)
Consumer가 특정 Offset까지 메시지를 성공적으로 처리했음을 Kafka에 알리는 것.
→ [Consumer Group & Offset](../../concepts/consumer-group-offset/#offset-커밋)

### Consumer
Kafka에서 메시지를 읽어가는 클라이언트 애플리케이션.
→ [핵심 구성요소](../../concepts/core-components/#2-consumer-소비자)

### Consumer Group
같은 목적을 가진 Consumer들의 논리적 그룹. 그룹 내에서 Partition이 분배됨.
→ [Consumer Group & Offset](../../concepts/consumer-group-offset/)

## D

### Dead Letter Topic (DLT)
처리에 실패한 메시지를 저장하는 별도의 Topic.
→ [기본 예제](../../examples/basic/#dead-letter-topic-dlt)

### Deserializer
바이트 배열을 객체로 변환하는 컴포넌트. `StringDeserializer`, `JsonDeserializer` 등.

## F

### Follower
Leader의 데이터를 복제하는 Broker. Leader 장애 시 새 Leader로 승격될 수 있음.
→ [Replication](../../concepts/replication/#leader와-follower)

## G

### Group ID
Consumer Group을 식별하는 고유 문자열. `spring.kafka.consumer.group-id`로 설정.

## I

### ISR (In-Sync Replicas)
Leader와 동기화된 Follower 집합. 메시지 안정성 보장에 중요.
→ [Replication](../../concepts/replication/#isr-in-sync-replicas)

## K

### KafkaListener
Spring Kafka의 Consumer 어노테이션. 특정 Topic의 메시지를 수신.
→ [기본 예제](../../examples/basic/#기본-kafkalistener)

### KafkaTemplate
Spring Kafka의 Producer 클래스. 메시지 전송에 사용.
→ [기본 예제](../../examples/basic/#kafkatemplate-주입)

### KRaft
Zookeeper 없이 Kafka 자체적으로 메타데이터를 관리하는 모드. Kafka 3.3+에서 권장.
→ [Replication](../../concepts/replication/#zookeeper-vs-kraft)

## L

### Leader
Partition의 읽기/쓰기를 담당하는 주 Broker. Producer와 Consumer는 Leader에만 연결.
→ [Replication](../../concepts/replication/#leader와-follower)

### Leader Election
Leader Broker 장애 시 ISR 중에서 새 Leader를 선출하는 과정.
→ [Replication](../../concepts/replication/#leader-election)

### Log Compaction
같은 Key의 메시지 중 최신 값만 유지하는 보관 정책.
→ [심화 개념](../../concepts/advanced-concepts/#log-compaction)

## M

### Message Key
메시지를 특정 Partition으로 라우팅하는 데 사용. 같은 Key는 같은 Partition으로.
→ [심화 개념](../../concepts/advanced-concepts/#message-key)

## O

### Offset
Partition 내 메시지의 순차적 위치 번호. 0부터 시작하여 증가.
→ [Consumer Group & Offset](../../concepts/consumer-group-offset/#offset이란)

## P

### Partition
Topic을 분할한 단위. 병렬 처리의 기본 단위.
→ [핵심 구성요소](../../concepts/core-components/#5-partition-파티션)

### Producer
Kafka에 메시지를 발행하는 클라이언트 애플리케이션.
→ [핵심 구성요소](../../concepts/core-components/#1-producer-생산자)

### Pull 방식
Consumer가 Broker에서 메시지를 가져오는 방식. Kafka는 Pull 방식 사용.
→ [메시지 흐름](../../concepts/message-flow/#pull-vs-push)

## R

### Rebalancing
Consumer Group 내에서 Partition을 재분배하는 과정. Consumer 추가/제거 시 발생.
→ [Consumer Group & Offset](../../concepts/consumer-group-offset/#리밸런싱-rebalancing)

### Replication Factor
각 Partition의 복제본 수. 프로덕션에서는 3 권장.
→ [Replication](../../concepts/replication/#replication-factor)

### Retention
메시지 보관 정책. 시간 기반, 용량 기반, Compaction 방식이 있음.
→ [심화 개념](../../concepts/advanced-concepts/#retention-보관-정책)

## S

### Serializer
객체를 바이트 배열로 변환하는 컴포넌트. `StringSerializer`, `JsonSerializer` 등.

## T

### Topic
메시지를 분류하는 논리적 채널. 관련 메시지들을 그룹화.
→ [핵심 구성요소](../../concepts/core-components/#4-topic-토픽)

## Z

### Zookeeper
Kafka 클러스터의 메타데이터를 관리하는 외부 서비스. KRaft 모드로 대체 중.
→ [Replication](../../concepts/replication/#zookeeper-vs-kraft)
