# AGENTS.md

> 이 파일은 Codex(AI 코딩 에이전트)가 이 프로젝트에서 작업할 때 반드시 따라야 할 지침서입니다.
> 코드를 작성하기 전에 반드시 이 파일 전체를 읽으세요.

---

## 1. Project Overview

**자동 가계부 앱** — Android 알림을 자동으로 파싱하여 소비 내역을 기록하는 가계부 앱.

- 카카오페이, 토스, 네이버페이, 카드사 등의 결제 알림을 `NotificationListenerService`로 감지
- 파싱된 데이터를 로컬 DB(Room)에 저장하고 백엔드 서버와 동기화
- 백엔드 API는 Swagger 문서 기반으로 연동 (직접 구현하지 않음)

---

## 2. Tech Stack

| 영역 | 기술 |
|------|------|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | MVVM + MVI (UiState 패턴) |
| DI | Hilt |
| Network | Retrofit2 + OkHttp3 + Kotlin Serialization |
| Local DB | Room + StateFlow |
| Notification | NotificationListenerService |
| Async | Coroutines + Flow |
| Navigation | Navigation Compose |
| Build | Gradle (Kotlin DSL) |

> ⚠️ Flutter, RxJava, LiveData, XML Layout은 절대 사용하지 마세요.

---

## 3. Architecture Rules

### 레이어 구조
```
presentation/   → Composable, ViewModel, UiState, UiEvent
domain/         → UseCase, Repository interface, Model
data/           → RepositoryImpl, remote(API/DTO), local(Room Entity/DAO)
```

### 규칙
- ViewModel은 반드시 `UiState`를 `StateFlow`로 노출한다
- Repository 인터페이스는 `domain` 레이어에, 구현체는 `data` 레이어에 위치한다
- UseCase는 단일 책임 원칙 — 하나의 UseCase는 하나의 동작만 수행한다
- Composable은 ViewModel을 직접 참조하지 않고 상위에서 state/event를 주입받는다
- 네트워크 DTO와 도메인 Model은 반드시 분리한다 (DTO → Model mapper 필수)

---

## 4. Notification Parsing Rules

- `NotificationListenerService`를 상속한 `TransactionNotificationService`에서만 알림을 처리한다
- 파싱 대상 앱 패키지 목록은 `NotificationParser` 클래스에서 관리한다
- 파싱 결과는 `TransactionEntity`로 변환하여 Room에 즉시 저장한다
- 파싱 실패 시 로그만 남기고 앱을 크래시시키지 않는다 (try-catch 필수)
- 알림 텍스트에서 추출할 필드: `amount`, `merchant`, `timestamp`, `paymentMethod`

---

## 5. API Integration Rules

- Swagger 문서를 기반으로 Retrofit 인터페이스를 생성한다
- API 응답은 `ApiResponse<T>` sealed class로 래핑하여 처리한다
- 인증 토큰은 `OkHttp Interceptor`에서 헤더에 자동 추가한다
- 네트워크 에러와 비즈니스 에러를 분리하여 처리한다
- DTO 클래스명은 `XxxRequestDto`, `XxxResponseDto` 형식을 따른다

---

## 6. Coding Conventions

- 함수명: 동사로 시작 (`getTransactions`, `parseNotification`)
- 클래스명: PascalCase, 역할이 명확하게 (`TransactionRepository`, `HomeViewModel`)
- `suspendRunCatching` 을 활용하여 코루틴 내 예외를 안전하게 처리한다
- 매직 넘버/문자열은 반드시 상수 또는 리소스로 추출한다
- TODO 주석은 이슈 번호와 함께 남긴다 (`// TODO #12: 카테고리 자동 분류 추가`)

---

## 7. What NOT To Do

- ❌ `runBlocking` 메인 스레드에서 사용 금지
- ❌ ViewModel에서 Context 직접 참조 금지 (ApplicationContext만 허용)
- ❌ Composable 내부에서 비즈니스 로직 처리 금지
- ❌ Repository에서 UI 관련 코드 참조 금지
- ❌ 하드코딩된 URL, API Key 금지 — 반드시 `local.properties` 또는 `BuildConfig` 사용
- ❌ `!!` (non-null assertion) 남용 금지 — `?.let` 또는 `?: return` 사용

---

## 8. Key Files Reference

| 파일 | 역할 |
|------|------|
| `ARCHITECTURE.md` | 모듈 구조 및 의존성 흐름 상세 설명 |
| `docs/design-docs/core-beliefs.md` | 제품 철학 및 UX 원칙 |
| `docs/product-specs/` | 기능별 요구사항 명세 |
| `docs/exec-plans/active/` | 현재 진행 중인 구현 계획 |
| `docs/generated/db-schema.md` | Room DB 스키마 자동 생성 문서 |
| `docs/references/` | 외부 라이브러리 레퍼런스 문서 |
