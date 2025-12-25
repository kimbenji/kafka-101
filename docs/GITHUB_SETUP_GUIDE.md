# GitHub Repository & Pages 설정 가이드

이 가이드는 `kafka-101` 프로젝트를 GitHub에 배포하기 위한 단계별 설정 방법입니다.

## 1. GitHub Repository 생성

### Step 1.1: 새 Repository 생성

1. GitHub에서 **New repository** 클릭
2. 설정:
   - **Repository name:** `kafka-101`
   - **Description:** Apache Kafka 학습 가이드
   - **Visibility:** Public (GitHub Pages 무료 사용을 위해)
   - **Initialize:** 체크하지 않음 (이미 로컬 프로젝트 있음)
3. **Create repository** 클릭

### Step 1.2: 로컬 프로젝트 연결

```bash
# 프로젝트 디렉토리에서 실행
cd /path/to/kafka-guidance-101

# Git 초기화 (이미 되어 있으면 스킵)
git init

# Remote 추가 (YOUR_USERNAME을 실제 사용자명으로 변경)
git remote add origin https://github.com/YOUR_USERNAME/kafka-101.git

# 확인
git remote -v
```

---

## 2. 플레이스홀더 수정

배포 전 아래 파일들의 `YOUR_USERNAME`을 실제 GitHub 사용자명으로 변경하세요.

### 수정 필요 파일

| 파일 | 수정 내용 |
|------|----------|
| `hugo.toml` | baseURL, editURL, menu URL |
| `README.md` | 문서 URL |

### 일괄 수정 명령어

```bash
# YOUR_USERNAME을 실제 사용자명으로 변경 (예: benji)
find . -name "*.toml" -o -name "README.md" | xargs sed -i 's/YOUR_USERNAME/실제사용자명/g'
```

또는 수동으로:

**hugo.toml:**
```toml
baseURL = 'https://benji.github.io/kafka-101/'
# ...
base = 'https://github.com/benji/kafka-101/edit/main/content'
# ...
url = "https://github.com/benji/kafka-101"
```

**README.md:**
```markdown
https://benji.github.io/kafka-101/
```

---

## 3. 첫 Push

```bash
# 모든 파일 스테이징
git add .

# 커밋
git commit -m "Initial commit: Kafka Guidance 101 프로젝트"

# main 브랜치로 푸시
git push -u origin main
```

---

## 4. GitHub Pages 설정

### Step 4.1: Pages 소스 설정

1. GitHub Repository 페이지 → **Settings** 탭
2. 좌측 메뉴에서 **Pages** 클릭
3. **Build and deployment** 섹션에서:
   - **Source:** `GitHub Actions` 선택 (Drop-down에서)

   > **중요:** "Deploy from a branch"가 아닌 **"GitHub Actions"**를 선택해야 합니다.
   > 이 프로젝트는 `.github/workflows/deploy.yml`에서 Hugo 빌드 후 배포합니다.

### Step 4.2: 첫 배포 확인

1. Repository → **Actions** 탭
2. "Deploy Hugo Site" 워크플로우가 자동 실행됨
3. 녹색 체크마크 확인 (성공)
4. **Settings → Pages** 에서 배포된 URL 확인:
   ```
   Your site is live at https://YOUR_USERNAME.github.io/kafka-101/
   ```

---

## 5. 배포 확인 체크리스트

| 항목 | 확인 |
|------|------|
| Repository 생성됨 | ☐ |
| Remote 연결됨 (`git remote -v`) | ☐ |
| 플레이스홀더 수정됨 | ☐ |
| 첫 Push 완료 | ☐ |
| Pages 소스 = GitHub Actions | ☐ |
| Actions 워크플로우 성공 (녹색) | ☐ |
| 사이트 접속 확인 | ☐ |

---

## 6. 트러블슈팅

### Actions 실패 시

1. **Actions** 탭에서 실패한 워크플로우 클릭
2. 로그 확인
3. 일반적인 원인:
   - Hugo 버전 불일치 → `.github/workflows/deploy.yml`의 `hugo-version` 확인
   - 테마 submodule 문제 → `git submodule update --init --recursive`

### 404 에러 시

1. **baseURL** 확인:
   ```toml
   baseURL = 'https://YOUR_USERNAME.github.io/kafka-101/'
   ```
   - 끝에 `/` 필수
   - Repository 이름과 일치해야 함

2. Pages 소스 확인:
   - Settings → Pages → Source가 "GitHub Actions"인지 확인

### 빈 페이지 시

1. `public/` 폴더가 `.gitignore`에 있는지 확인 (있어야 함)
2. Actions 로그에서 Hugo 빌드 출력 확인

---

## 7. 최종 URL 구조

설정 완료 후 접근 가능한 URL:

| 페이지 | URL |
|--------|-----|
| 홈 | `https://USERNAME.github.io/kafka-101/` |
| 문서 | `https://USERNAME.github.io/kafka-101/docs/` |
| Quick Start | `https://USERNAME.github.io/kafka-101/docs/quick-start/` |
| 개념 이해 | `https://USERNAME.github.io/kafka-101/docs/concepts/` |

---

## 참고: Repository 이름 변경 시

Repository 이름을 `kafka-101`이 아닌 다른 이름으로 사용하려면:

1. `hugo.toml`의 모든 URL 경로 수정
2. `README.md`의 URL 수정
3. `.github/workflows/deploy.yml`은 동적으로 baseURL을 가져오므로 수정 불필요

```toml
# 예: repository 이름이 "kafka-guide"인 경우
baseURL = 'https://USERNAME.github.io/kafka-guide/'
```
