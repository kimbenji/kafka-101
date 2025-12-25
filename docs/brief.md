# Project Brief: Kafka Guidance 101

## Executive Summary

**Kafka Guidance 101**은 Apache Kafka를 Java/Spring Boot 환경에서 활용하는 방법을 다루는 종합 가이드 문서 프로젝트입니다. 이 프로젝트는 Kafka의 핵심 개념부터 실무 운영까지를 체계적으로 다루며, 실행 가능한 예제 코드를 포함하여 학습 효과를 극대화합니다.

- **주요 목적:** Kafka 개념 이해 및 Spring Boot 통합 실습 가이드 제공
- **대상 독자:** Kafka 입문자, Spring Boot 개발자, 자기 학습 목적 개발자
- **배포 방식:** GitHub Pages + Hugo 정적 사이트

---

## Problem Statement

### 현재 상태 및 문제점

1. **분산된 학습 자료:** Kafka 관련 자료가 공식 문서, 블로그, 유튜브 등에 분산되어 있어 체계적 학습이 어려움
2. **이론과 실습의 괴리:** 개념 설명 위주의 자료가 많아 실제 코드로 구현하는 데 어려움
3. **Spring Boot 통합 가이드 부족:** Kafka 자체 문서는 많지만 Spring Boot와의 통합 실습 자료는 상대적으로 부족
4. **한국어 자료 부족:** 양질의 한국어 Kafka 가이드가 제한적

### 해결의 필요성

- Kafka는 현대 분산 시스템의 핵심 인프라로, 백엔드 개발자에게 필수 역량
- 체계적인 학습 자료가 있으면 학습 시간 단축 및 이해도 향상 가능
- 실행 가능한 예제가 있으면 "따라하면서 배우기" 가능

---

## Proposed Solution

### 핵심 접근 방식

**개념 → 실습 → 심화 → 운영**의 단계적 학습 구조를 가진 종합 가이드 문서를 제작합니다.

### 주요 특징

1. **First Principles 기반 개념 설명:** 왜 필요한지부터 설명하여 본질적 이해 유도
2. **실행 가능한 예제 코드:** 모든 예제는 Spring Boot 프로젝트로 직접 실행 가능
3. **Mermaid 다이어그램:** 시각적 이해를 위한 다이어그램 포함
4. **Hugo + GitHub Pages:** 접근성 높은 정적 사이트로 배포

### 차별점

- 단순 개념 나열이 아닌 "왜 이렇게 동작하는가"에 집중
- 모든 예제가 실제 실행 가능 (Docker Compose 포함)
- 학습 효과를 높이는 부가 자료 (FAQ, 연습문제, 용어사전)

---

## Target Users

### Primary User Segment: Kafka 입문자 & Spring Boot 개발자

**프로필:**
- Java/Spring Boot 개발 경험 있음
- Kafka는 처음이거나 기초 수준
- 실무에서 Kafka를 사용해야 하거나 관심이 있음

**현재 행동:**
- 공식 문서와 블로그를 검색하며 학습
- 단편적인 코드 예제를 복사하여 사용
- 개념 이해 없이 코드만 따라하는 경우 많음

**니즈:**
- 체계적인 개념 정리
- 바로 실행할 수 있는 예제 코드
- 실무에서 발생하는 문제 해결 방법

### Secondary User Segment: 자기 학습 목적 (본인 포함)

**프로필:**
- 깊은 이해를 원하는 학습자
- 나중에 참고할 수 있는 문서가 필요

**니즈:**
- 개념의 본질적 이해 (왜 이렇게 동작하는가)
- 다시 볼 때 빠르게 찾을 수 있는 구조
- 연습문제로 이해도 확인

---

## Goals & Success Metrics

### Business Objectives

- GitHub Pages에 성공적으로 배포하여 누구나 접근 가능하게 함
- 체계적인 Kafka 학습 자료 완성
- 본인의 Kafka 이해도 향상

### User Success Metrics

- 가이드를 따라 Spring Boot + Kafka 프로젝트를 처음부터 구현 가능
- Kafka 핵심 개념을 다른 사람에게 설명 가능
- 실무에서 Kafka 관련 문제 발생 시 참고 자료로 활용 가능

### Key Performance Indicators (KPIs)

- **문서 완성도:** 계획된 모든 섹션 작성 완료
- **예제 동작 여부:** 모든 예제 코드가 정상 실행
- **배포 성공:** GitHub Pages에서 정상 접근 가능

---

## MVP Scope

### Core Features (Must Have)

- **0. Quick Start:** Docker Compose + Spring Boot로 5분 만에 시작하기
- **1. 개념 이해:**
  - 1.1 핵심 구성요소 (Producer, Consumer, Broker, Topic, Partition)
  - 1.2 메시지 흐름 (발행, 저장, 소비)
  - 1.3 심화 개념 (Consumer Group, Offset, Replication, acks, Message Key, Retention)
- **2. 실습 예제:**
  - 2.0 환경 구성 (Docker Compose, Spring Boot 설정)
  - 2.1 기본 Producer/Consumer
  - 2.2 주문 시스템 예제
- **부록:**
  - 용어 사전
  - 참고 자료 링크

### Out of Scope for MVP

- 2.3 이벤트 소싱 패턴
- 2.4 에러 처리 & 재시도
- 2.5 여러 Consumer Group
- 3. 심화 주제 (성능 튜닝, 트랜잭션, Schema Registry, Kafka Streams, Kafka Connect, 보안)
- 4. 운영 가이드 (모니터링, 클러스터 관리, 백업, 장애 대응, 용량 계획)
- FAQ
- 연습 문제

### MVP Success Criteria

- 기본 개념 섹션 완성
- Quick Start로 5분 안에 메시지 송수신 가능
- 주문 시스템 예제 정상 동작
- GitHub Pages 배포 완료

---

## Post-MVP Vision

### Phase 2 Features

- 2.3 이벤트 소싱 패턴
- 2.4 에러 처리 & 재시도 (Dead Letter Topic)
- 2.5 여러 Consumer Group
- 3.1 성능 튜닝
- 3.2 트랜잭션 (Exactly-once)
- FAQ 및 연습 문제

### Phase 3 Features

- 3.3 Schema Registry
- 3.4 Kafka Streams
- 3.5 Kafka Connect
- 3.6 보안 설정
- 4. 운영 가이드 전체

### Long-term Vision

- 한국어로 된 최고의 Kafka 실무 가이드
- 지속적인 업데이트 (Kafka 버전 업그레이드 반영)
- 커뮤니티 피드백 반영

### Expansion Opportunities

- 영어 버전 제작
- 동영상 강의 연계
- 실습 환경 제공 (GitHub Codespaces 등)

---

## Technical Considerations

### Platform Requirements

- **Target Platforms:** 웹 브라우저 (정적 사이트)
- **Hosting:** GitHub Pages
- **정적 사이트 생성기:** Hugo

### Technology Preferences

- **문서:** Markdown + Hugo
- **다이어그램:** Mermaid.js
- **예제 코드:**
  - Java 17+
  - Spring Boot 3.x
  - Spring Kafka
- **로컬 개발 환경:**
  - Docker Compose
  - Kafka (KRaft 모드 권장)

### Architecture Considerations

- **Repository Structure:**
  ```
  kafka-guidance-101/
  ├── docs/                 # 문서 소스
  ├── examples/             # 예제 프로젝트
  │   ├── quick-start/
  │   ├── order-system/
  │   └── ...
  ├── hugo/                 # Hugo 사이트
  └── docker/               # Docker Compose 파일
  ```
- **Hugo Theme:** 문서에 적합한 테마 선택 (예: Docsy, Book)
- **CI/CD:** GitHub Actions로 자동 배포

---

## Constraints & Assumptions

### Constraints

- **Budget:** 무료 (GitHub Pages, 오픈소스 도구만 사용)
- **Timeline:** 개인 프로젝트로 여유롭게 진행
- **Resources:** 1인 개발
- **Technical:** Hugo + GitHub Pages 제약 내에서 작업

### Key Assumptions

- 독자는 Java/Spring Boot 기본 지식이 있음
- Docker 사용 가능한 환경
- Kafka 클러스터는 로컬 Docker로 구성 (운영 환경 별도)
- KRaft 모드 사용 (Zookeeper 미사용)

---

## Risks & Open Questions

### Key Risks

- **범위 확대:** 모든 주제를 다루려다 완성하지 못할 위험 → MVP 범위 명확화로 대응
- **예제 유지보수:** Spring Boot/Kafka 버전 업그레이드 시 예제 깨짐 → 버전 고정 및 정기 업데이트
- **동기 부여 저하:** 개인 프로젝트로 동기 부여 어려움 → 단계별 목표 설정

### Open Questions

- Hugo 테마 선택 (Docsy vs Book vs 기타)
- 예제 코드 저장소 구조 (모노레포 vs 멀티레포)
- Kafka 버전 (최신 LTS 기준)

### Areas Needing Further Research

- Hugo + Mermaid.js 통합 방법
- GitHub Actions 워크플로우 설정
- 효과적인 연습문제 설계

---

## Appendices

### A. Research Summary

**브레인스토밍 세션 결과 (First Principles Thinking):**

Kafka의 본질적 문제:
> 비동기 혹은 준실시간 고용량 데이터 처리를 위한 고가용성 인프라

핵심 개념 매핑:
| 본질적 문제 | 해결 요소 |
|-------------|-----------|
| 고용량 처리 | Partition, Topic |
| 고가용성 | Broker, Replication |
| 비동기/준실시간 | Producer, Consumer |

**Mind Mapping 결과:**
- 6개 주요 섹션 도출
- Role Playing으로 Quick Start 및 부록 추가

### B. Brainstorming Session Reference

- 세션 결과 문서: `docs/brainstorming-session-results.md`

### C. References

- [Apache Kafka 공식 문서](https://kafka.apache.org/documentation/)
- [Spring for Apache Kafka](https://spring.io/projects/spring-kafka)
- [Hugo Documentation](https://gohugo.io/documentation/)

---

## Next Steps

### Immediate Actions

1. Hugo 프로젝트 초기화 및 테마 선택
2. 예제 프로젝트 구조 설정
3. Docker Compose 파일 작성 (Kafka KRaft)
4. Quick Start 섹션 작성 시작

### PM Handoff

이 Project Brief는 **Kafka Guidance 101** 프로젝트의 전체 맥락을 제공합니다. PRD 작성 시 이 문서를 참고하여 상세 요구사항을 정의하세요.

---

*Generated from Brainstorming Session Results*
*Project: Kafka Guidance 101*
