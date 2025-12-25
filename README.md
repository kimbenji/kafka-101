# Kafka Guidance 101

Apache Kafka를 Java/Spring Boot 환경에서 활용하는 종합 가이드입니다.

## 문서 보기

https://YOUR_USERNAME.github.io/kafka-101/

> **Note:** 배포 전 위 URL의 `YOUR_USERNAME`을 실제 GitHub 사용자명으로 변경하세요.

## 로컬 개발 환경

### 사전 요구사항

- Docker & Docker Compose
- Java 17+
- Hugo (Extended) 0.153.2+

### 문서 사이트 로컬 실행

```bash
# Hugo 서버 시작
hugo server -D

# 브라우저에서 http://localhost:1313 접속
```

### Kafka 환경 시작

```bash
cd docker
docker-compose up -d

# Kafka 상태 확인
docker-compose ps
```

### 예제 프로젝트 실행

```bash
# Quick Start 예제
cd examples/quick-start
./gradlew bootRun

# 메시지 전송 테스트
curl -X POST "http://localhost:8080/send?message=Hello"
```

## 프로젝트 구조

```
kafka-guidance-101/
├── content/docs/       # Hugo 문서 소스 (Markdown)
│   ├── quick-start/    # 5분 Quick Start
│   ├── concepts/       # Kafka 핵심 개념
│   ├── examples/       # 실습 예제 가이드
│   └── appendix/       # 용어 사전, 참고 자료
├── examples/           # Spring Boot 예제 프로젝트
│   ├── quick-start/    # 최소 설정 예제
│   └── order-system/   # 주문 시스템 예제
├── docker/             # Kafka Docker Compose
└── .github/workflows/  # GitHub Actions 배포
```

## 기술 스택

| 분류 | 기술 | 버전 |
|-----|------|------|
| 문서 | Hugo + PaperMod | 0.153.2 |
| 다이어그램 | Mermaid.js | 10.x |
| 예제 | Spring Boot | 3.2.x |
| 메시징 | Apache Kafka (KRaft) | 3.6.x |

## 배포

main 브랜치에 push하면 GitHub Actions가 자동으로 GitHub Pages에 배포합니다.

## 라이선스

MIT License

## 기여

이슈 및 PR 환영합니다.
