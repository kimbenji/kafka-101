# Kafka Guidance 101 Product Requirements Document (PRD)

## Goals and Background Context

### Goals

- Kafka 핵심 개념을 체계적으로 이해할 수 있는 문서 제공
- Spring Boot 환경에서 바로 실행 가능한 예제 코드 제공
- 5분 안에 Kafka 메시지 송수신을 경험할 수 있는 Quick Start 제공
- GitHub Pages에서 접근 가능한 정적 사이트로 배포
- 학습 효과를 높이는 보조 자료 (용어 사전, 참고 자료) 제공

### Background Context

Apache Kafka는 현대 분산 시스템의 핵심 인프라로, 백엔드 개발자에게 필수 역량이 되었습니다. 그러나 양질의 한국어 학습 자료가 부족하고, 기존 자료들은 개념 설명 위주로 실제 코드 구현과 괴리가 있습니다.

이 프로젝트는 "First Principles" 접근 방식으로 Kafka의 본질적 개념을 설명하고, Spring Boot와 통합된 실행 가능한 예제를 제공하여 "따라하면서 배우는" 학습 경험을 제공합니다. Hugo 정적 사이트 생성기와 GitHub Pages를 활용하여 누구나 무료로 접근할 수 있는 가이드를 목표로 합니다.

### Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2024-12-25 | 0.1 | 초안 작성 | BMad Master |

---

## Requirements

### Functional Requirements

#### 문서 콘텐츠

- **FR1:** Quick Start 섹션은 Docker Compose를 사용하여 5분 안에 Kafka 환경을 구성하고 메시지 송수신을 확인할 수 있어야 한다.
- **FR2:** 개념 이해 섹션은 Producer, Consumer, Broker, Topic, Partition의 핵심 구성요소를 Mermaid 다이어그램과 함께 설명해야 한다.
- **FR3:** 개념 이해 섹션은 메시지 흐름 (발행 → 저장 → 소비)을 시각적으로 설명해야 한다.
- **FR4:** 심화 개념 섹션은 Consumer Group, Offset, Replication, acks, Message Key, Retention을 다루어야 한다.
- **FR5:** 환경 구성 섹션은 Docker Compose 파일과 Spring Boot 프로젝트 설정 방법을 제공해야 한다.
- **FR6:** 기본 Producer/Consumer 예제는 단순 메시지 전송 및 수신 코드를 포함해야 한다.
- **FR7:** 주문 시스템 예제는 실무에 가까운 시나리오 (주문 생성, 결제, 배송)를 구현해야 한다.
- **FR8:** 용어 사전은 Kafka 핵심 용어를 정의하고 설명해야 한다.
- **FR9:** 참고 자료 섹션은 공식 문서 및 추가 학습 자료 링크를 제공해야 한다.

#### 예제 코드

- **FR10:** 모든 예제 코드는 Java 17+ 및 Spring Boot 3.x 환경에서 실행 가능해야 한다.
- **FR11:** 예제 프로젝트는 독립적으로 실행 가능한 완전한 프로젝트여야 한다.
- **FR12:** Docker Compose 파일은 Kafka를 KRaft 모드로 실행해야 한다 (Zookeeper 미사용).
- **FR13:** 각 예제는 README를 포함하여 실행 방법을 설명해야 한다.

#### 다이어그램

- **FR14:** 모든 개념 설명에는 Mermaid.js 다이어그램이 포함되어야 한다.
- **FR15:** 다이어그램은 Hugo 사이트에서 정상 렌더링되어야 한다.

### Non-Functional Requirements

#### 배포 및 접근성

- **NFR1:** 사이트는 GitHub Pages에서 호스팅되어야 한다.
- **NFR2:** Hugo 정적 사이트 생성기를 사용해야 한다.
- **NFR3:** 사이트는 모바일 및 데스크톱에서 반응형으로 동작해야 한다.
- **NFR4:** 페이지 로딩 시간은 3초 이내여야 한다.

#### 콘텐츠 품질

- **NFR5:** 모든 문서는 한국어로 작성되어야 한다 (기술 용어는 영어 허용).
- **NFR6:** 문서는 일관된 스타일과 톤을 유지해야 한다.
- **NFR7:** 코드 예제는 복사-붙여넣기로 바로 사용 가능해야 한다.

#### 유지보수

- **NFR8:** GitHub Actions를 사용하여 자동 배포해야 한다.
- **NFR9:** 문서 소스는 Markdown 형식이어야 한다.

---

## Technical Assumptions

### Repository Structure: Monorepo

```
kafka-guidance-101/
├── content/              # Hugo 콘텐츠 (Markdown)
│   ├── docs/
│   │   ├── _index.md
│   │   ├── quick-start/
│   │   ├── concepts/
│   │   ├── examples/
│   │   └── appendix/
├── examples/             # 예제 프로젝트
│   ├── quick-start/
│   └── order-system/
├── static/               # 정적 자산
├── docker/               # Docker Compose 파일
├── themes/               # Hugo 테마
├── config.toml           # Hugo 설정
└── .github/
    └── workflows/        # GitHub Actions
```

### Service Architecture

- **정적 사이트:** Hugo로 생성된 정적 HTML/CSS/JS
- **호스팅:** GitHub Pages (무료)
- **예제 애플리케이션:** 독립적인 Spring Boot 프로젝트들
- **로컬 인프라:** Docker Compose (Kafka KRaft 모드)

### Testing Requirements

- **문서:** 링크 유효성 검사, Mermaid 렌더링 확인
- **예제 코드:** 각 예제 프로젝트의 빌드 및 실행 테스트
- **배포:** GitHub Pages 배포 후 사이트 접근 확인

### Additional Technical Assumptions

- Hugo 테마: 문서에 적합한 테마 선택 (Book, Docsy 등 검토)
- Mermaid.js: Hugo 테마 또는 shortcode로 통합
- Kafka 버전: 최신 LTS (3.x)
- Spring Boot 버전: 3.x
- Java 버전: 17+
- Spring Kafka: spring-kafka 최신 버전

---

## Epic List

| Epic | 제목 | 목표 |
|------|------|------|
| **Epic 1** | 프로젝트 기반 구축 | Hugo 사이트 초기화, GitHub Actions 설정, 기본 구조 확립 |
| **Epic 2** | Quick Start 완성 | 5분 만에 Kafka 체험할 수 있는 가이드 작성 |
| **Epic 3** | 개념 이해 섹션 | Kafka 핵심 개념 및 심화 개념 문서화 |
| **Epic 4** | 실습 예제 확장 | 주문 시스템 예제 및 추가 실습 작성 |
| **Epic 5** | 부록 및 마무리 | 용어 사전, 참고 자료, 최종 검토 |

---

## Epic 1: 프로젝트 기반 구축

**목표:** Hugo 프로젝트를 초기화하고, GitHub Actions를 통한 자동 배포 파이프라인을 구축하며, 프로젝트의 기본 구조를 확립합니다. 이 Epic 완료 시 빈 사이트가 GitHub Pages에 배포되어 접근 가능해야 합니다.

### Story 1.1: Hugo 프로젝트 초기화

**As a** 개발자,
**I want** Hugo 프로젝트를 초기화하고 적합한 테마를 설정하고 싶다,
**So that** 문서 사이트의 기본 구조를 갖출 수 있다.

**Acceptance Criteria:**
1. Hugo 프로젝트가 초기화되어 있다
2. 문서에 적합한 테마가 적용되어 있다 (Book 또는 Docsy)
3. config.toml에 기본 설정이 완료되어 있다
4. 로컬에서 `hugo server`로 사이트 확인 가능하다
5. 메인 페이지에 프로젝트 제목과 간단한 소개가 표시된다

### Story 1.2: 프로젝트 디렉토리 구조 설정

**As a** 개발자,
**I want** 프로젝트의 디렉토리 구조를 설정하고 싶다,
**So that** 문서와 예제 코드를 체계적으로 관리할 수 있다.

**Acceptance Criteria:**
1. content/docs/ 디렉토리 구조가 생성되어 있다 (quick-start, concepts, examples, appendix)
2. examples/ 디렉토리가 생성되어 있다
3. docker/ 디렉토리가 생성되어 있다
4. 각 섹션에 _index.md 파일이 있다
5. 사이드바 네비게이션에 섹션 구조가 반영된다

### Story 1.3: GitHub Actions 배포 설정

**As a** 개발자,
**I want** GitHub Actions를 통한 자동 배포를 설정하고 싶다,
**So that** main 브랜치에 푸시하면 자동으로 GitHub Pages에 배포된다.

**Acceptance Criteria:**
1. .github/workflows/deploy.yml 파일이 생성되어 있다
2. main 브랜치 푸시 시 자동으로 빌드 및 배포가 실행된다
3. GitHub Pages 설정이 완료되어 있다
4. 배포된 사이트에 접근 가능하다
5. 빌드 실패 시 알림이 발생한다

### Story 1.4: Mermaid.js 통합

**As a** 독자,
**I want** Mermaid 다이어그램이 문서에서 정상 렌더링되기를 원한다,
**So that** 시각적으로 개념을 이해할 수 있다.

**Acceptance Criteria:**
1. Hugo 테마 또는 shortcode로 Mermaid.js가 통합되어 있다
2. Markdown 내 mermaid 코드 블록이 다이어그램으로 렌더링된다
3. 테스트용 다이어그램이 포함된 샘플 페이지가 있다
4. 로컬 및 배포 환경 모두에서 정상 동작한다

---

## Epic 2: Quick Start 완성

**목표:** Docker Compose로 Kafka 환경을 구성하고, Spring Boot Producer/Consumer를 실행하여 5분 안에 메시지 송수신을 경험할 수 있는 가이드를 완성합니다.

### Story 2.1: Docker Compose 파일 작성

**As a** 개발자,
**I want** Kafka를 KRaft 모드로 실행하는 Docker Compose 파일을 원한다,
**So that** 로컬에서 쉽게 Kafka 환경을 구성할 수 있다.

**Acceptance Criteria:**
1. docker/docker-compose.yml 파일이 생성되어 있다
2. Kafka가 KRaft 모드로 실행된다 (Zookeeper 없음)
3. `docker-compose up -d`로 Kafka가 정상 시작된다
4. localhost:9092로 Kafka에 접근 가능하다
5. 종료 및 재시작이 정상 동작한다

### Story 2.2: Quick Start Spring Boot 프로젝트 생성

**As a** 개발자,
**I want** 최소한의 설정으로 동작하는 Spring Boot Kafka 예제를 원한다,
**So that** 빠르게 메시지 송수신을 테스트할 수 있다.

**Acceptance Criteria:**
1. examples/quick-start/ 디렉토리에 Spring Boot 프로젝트가 있다
2. Producer가 메시지를 전송할 수 있다
3. Consumer가 메시지를 수신하고 로그로 출력한다
4. application.yml에 Kafka 설정이 포함되어 있다
5. README.md에 실행 방법이 설명되어 있다

### Story 2.3: Quick Start 문서 작성

**As a** Kafka 입문자,
**I want** 5분 안에 Kafka를 체험할 수 있는 가이드를 원한다,
**So that** 복잡한 개념 없이 빠르게 시작할 수 있다.

**Acceptance Criteria:**
1. content/docs/quick-start/ 문서가 작성되어 있다
2. Docker Compose 실행 방법이 설명되어 있다
3. Spring Boot 프로젝트 실행 방법이 설명되어 있다
4. 메시지 전송 및 수신 확인 방법이 설명되어 있다
5. 예상 결과 (로그 출력)가 포함되어 있다
6. 트러블슈팅 팁이 포함되어 있다

---

## Epic 3: 개념 이해 섹션

**목표:** Kafka의 핵심 구성요소, 메시지 흐름, 심화 개념을 Mermaid 다이어그램과 함께 체계적으로 문서화합니다.

### Story 3.1: 핵심 구성요소 문서 작성

**As a** Kafka 입문자,
**I want** Producer, Consumer, Broker, Topic, Partition에 대한 명확한 설명을 원한다,
**So that** Kafka의 기본 구조를 이해할 수 있다.

**Acceptance Criteria:**
1. content/docs/concepts/core-components.md 문서가 작성되어 있다
2. 각 구성요소의 역할이 명확히 설명되어 있다
3. 구성요소 간의 관계를 보여주는 Mermaid 다이어그램이 포함되어 있다
4. Kafka가 해결하는 본질적 문제 (비동기, 고용량, 고가용성)와 연결되어 있다
5. 간단한 비유나 예시가 포함되어 있다

### Story 3.2: 메시지 흐름 문서 작성

**As a** Kafka 입문자,
**I want** 메시지가 발행되고 소비되는 전체 흐름을 이해하고 싶다,
**So that** Kafka가 어떻게 동작하는지 파악할 수 있다.

**Acceptance Criteria:**
1. content/docs/concepts/message-flow.md 문서가 작성되어 있다
2. 발행 과정이 설명되어 있다
3. 저장 구조 (Topic, Partition, Offset)가 설명되어 있다
4. 소비 과정이 설명되어 있다
5. 전체 흐름을 보여주는 시퀀스 다이어그램이 포함되어 있다

### Story 3.3: Consumer Group & Offset 문서 작성

**As a** Kafka 학습자,
**I want** Consumer Group과 Offset 개념을 이해하고 싶다,
**So that** 병렬 처리와 장애 복구 메커니즘을 알 수 있다.

**Acceptance Criteria:**
1. content/docs/concepts/consumer-group-offset.md 문서가 작성되어 있다
2. Consumer Group의 역할과 규칙이 설명되어 있다
3. Offset의 개념과 커밋 방식이 설명되어 있다
4. 관련 Mermaid 다이어그램이 포함되어 있다
5. 장애 복구 시나리오가 설명되어 있다

### Story 3.4: Replication & Leader Election 문서 작성

**As a** Kafka 학습자,
**I want** 데이터 복제와 리더 선출 메커니즘을 이해하고 싶다,
**So that** 고가용성이 어떻게 보장되는지 알 수 있다.

**Acceptance Criteria:**
1. content/docs/concepts/replication.md 문서가 작성되어 있다
2. Leader/Follower 개념이 설명되어 있다
3. Replication Factor와 ISR이 설명되어 있다
4. Leader Election 과정이 설명되어 있다
5. Zookeeper vs KRaft 비교가 포함되어 있다
6. 관련 Mermaid 다이어그램이 포함되어 있다

### Story 3.5: acks & Message Key & Retention 문서 작성

**As a** Kafka 학습자,
**I want** 전송 보장, 파티셔닝, 보관 정책을 이해하고 싶다,
**So that** 실무에서 적절한 설정을 선택할 수 있다.

**Acceptance Criteria:**
1. content/docs/concepts/advanced-concepts.md 문서가 작성되어 있다
2. acks 옵션 (0, 1, all)과 Trade-off가 설명되어 있다
3. Message Key와 파티션 결정 방식이 설명되어 있다
4. Retention 정책 (시간, 용량, compaction)이 설명되어 있다
5. 각 개념에 적합한 사용 사례가 포함되어 있다

---

## Epic 4: 실습 예제 확장

**목표:** 환경 구성 상세 가이드와 주문 시스템 예제를 작성하여 실무에 가까운 학습 경험을 제공합니다.

### Story 4.1: 환경 구성 상세 문서 작성

**As a** Spring Boot 개발자,
**I want** Kafka 연동을 위한 상세한 Spring Boot 설정 방법을 원한다,
**So that** 내 프로젝트에 Kafka를 적용할 수 있다.

**Acceptance Criteria:**
1. content/docs/examples/setup.md 문서가 작성되어 있다
2. Spring Boot 의존성 설정이 설명되어 있다
3. application.yml 설정이 상세히 설명되어 있다
4. Producer/Consumer 설정 옵션이 설명되어 있다
5. 일반적인 설정 오류와 해결 방법이 포함되어 있다

### Story 4.2: 기본 Producer/Consumer 예제 문서

**As a** Spring Boot 개발자,
**I want** 기본적인 Producer/Consumer 구현 방법을 상세히 알고 싶다,
**So that** 코드 작성 방법을 익힐 수 있다.

**Acceptance Criteria:**
1. content/docs/examples/basic.md 문서가 작성되어 있다
2. KafkaTemplate을 사용한 Producer 코드가 설명되어 있다
3. @KafkaListener를 사용한 Consumer 코드가 설명되어 있다
4. 동기/비동기 전송 방식이 설명되어 있다
5. 코드에 대한 상세 설명이 포함되어 있다

### Story 4.3: 주문 시스템 예제 프로젝트 생성

**As a** 개발자,
**I want** 실무에 가까운 주문 시스템 예제를 원한다,
**So that** 실제 사용 사례를 경험할 수 있다.

**Acceptance Criteria:**
1. examples/order-system/ 디렉토리에 프로젝트가 있다
2. 주문 생성 이벤트 Producer가 구현되어 있다
3. 주문 처리 Consumer가 구현되어 있다
4. 여러 이벤트 타입 (주문생성, 결제완료, 배송시작)이 구현되어 있다
5. Message Key를 사용한 순서 보장이 구현되어 있다
6. README.md에 실행 방법이 설명되어 있다

### Story 4.4: 주문 시스템 예제 문서 작성

**As a** Kafka 학습자,
**I want** 주문 시스템 예제에 대한 상세한 설명을 원한다,
**So that** 코드를 이해하고 응용할 수 있다.

**Acceptance Criteria:**
1. content/docs/examples/order-system.md 문서가 작성되어 있다
2. 시스템 아키텍처가 다이어그램으로 설명되어 있다
3. 각 이벤트 타입과 처리 로직이 설명되어 있다
4. Message Key 사용 이유가 설명되어 있다
5. 실행 방법과 결과 확인 방법이 포함되어 있다

---

## Epic 5: 부록 및 마무리

**목표:** 용어 사전, 참고 자료를 작성하고, 전체 문서를 검토하여 최종 완성합니다.

### Story 5.1: 용어 사전 작성

**As a** Kafka 입문자,
**I want** Kafka 용어를 빠르게 찾아볼 수 있는 사전을 원한다,
**So that** 문서를 읽다가 모르는 용어를 확인할 수 있다.

**Acceptance Criteria:**
1. content/docs/appendix/glossary.md 문서가 작성되어 있다
2. 주요 Kafka 용어가 알파벳/가나다 순으로 정리되어 있다
3. 각 용어에 간결한 정의와 설명이 포함되어 있다
4. 관련 문서 섹션으로의 링크가 포함되어 있다

### Story 5.2: 참고 자료 페이지 작성

**As a** Kafka 학습자,
**I want** 추가 학습을 위한 참고 자료 목록을 원한다,
**So that** 더 깊이 공부할 수 있다.

**Acceptance Criteria:**
1. content/docs/appendix/references.md 문서가 작성되어 있다
2. Apache Kafka 공식 문서 링크가 포함되어 있다
3. Spring for Apache Kafka 문서 링크가 포함되어 있다
4. 추천 서적/강의가 포함되어 있다
5. 커뮤니티 리소스 (블로그, 유튜브 등)가 포함되어 있다

### Story 5.3: 최종 검토 및 수정

**As a** 프로젝트 관리자,
**I want** 전체 문서를 검토하고 일관성을 확인하고 싶다,
**So that** 품질 높은 가이드를 제공할 수 있다.

**Acceptance Criteria:**
1. 모든 내부 링크가 유효하다
2. 모든 Mermaid 다이어그램이 정상 렌더링된다
3. 모든 예제 코드가 정상 실행된다
4. 문서 스타일이 일관성 있다
5. 오탈자가 수정되어 있다
6. GitHub Pages에서 최종 배포가 완료되어 있다

---

## Next Steps

### Architect Prompt

이 PRD를 기반으로 Architecture 문서를 생성해 주세요. 특히 다음 사항에 집중해 주세요:
- Hugo 프로젝트 구조 상세 설계
- GitHub Actions 워크플로우 설계
- 예제 프로젝트 구조 설계
- Mermaid.js 통합 방법

### Development Prompt

Epic 1부터 순차적으로 Story를 구현해 주세요. 각 Story는 독립적으로 완료 가능하며, Story 완료 시 GitHub에 커밋해 주세요.

---

*Generated from Project Brief: Kafka Guidance 101*
