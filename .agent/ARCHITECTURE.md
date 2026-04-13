# ARCHITECTURE.md

> 앱의 모듈 구조, 레이어 의존성, 데이터 흐름을 정의합니다.
> Codex는 새로운 기능을 추가할 때 이 구조를 반드시 따르세요.

---

## 1. 모듈 구조

```
app/
├── src/main/
│   ├── java/com/yourpackage/
│   │   ├── presentation/
│   │   │   ├── home/
│   │   │   ├── transaction/
│   │   │   ├── statistics/
│   │   │   ├── settings/
│   │   │   └── common/         ← 공통 Composable, UiState base
│   │   ├── domain/
│   │   │   ├── model/          ← 도메인 모델 (DTO 아님)
│   │   │   ├── repository/     ← Repository 인터페이스
│   │   │   └── usecase/
│   │   ├── data/
│   │   │   ├── remote/
│   │   │   │   ├── api/        ← Retrofit interface
│   │   │   │   └── dto/        ← Request/Response DTO
│   │   │   ├── local/
│   │   │   │   ├── dao/
│   │   │   │   ├── entity/
│   │   │   │   └── database/
│   │   │   ├── mapper/         ← DTO ↔ Model 변환
│   │   │   └── repository/     ← RepositoryImpl
│   │   ├── service/
│   │   │   └── TransactionNotificationService.kt
│   │   └── di/                 ← Hilt Module
```

---

## 2. 레이어 의존성

```
presentation → domain ← data
```

- `presentation`은 `domain`만 참조한다
- `data`는 `domain` 인터페이스를 구현한다
- 레이어 간 역방향 참조는 절대 금지

---

## 3. 데이터 흐름

### 알림 파싱 흐름
```
NotificationListenerService
    → NotificationParser (텍스트 파싱)
    → TransactionEntity (Room 저장)
    → TransactionRepository (서버 동기화)
    → ViewModel (StateFlow 업데이트)
    → UI (Compose 리컴포지션)
```

### 일반 API 흐름
```
UI Event
    → ViewModel
    → UseCase
    → RepositoryImpl
    → Retrofit API / Room DAO
    → Flow<Result<T>>
    → ViewModel UiState 업데이트
    → UI 리컴포지션
```

---

## 4. UiState 패턴

모든 ViewModel은 아래 패턴을 따른다.

```kotlin
data class HomeUiState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class HomeUiEvent {
    data class DeleteTransaction(val id: Long) : HomeUiEvent()
    object Refresh : HomeUiEvent()
}

class HomeViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onEvent(event: HomeUiEvent) { ... }
}
```

---

## 5. Room DB 구조

### 주요 Entity
| Entity | 설명 |
|--------|------|
| `TransactionEntity` | 결제 내역 (파싱 결과) |
| `CategoryEntity` | 카테고리 분류 |
| `BudgetEntity` | 월별 예산 설정 |

### 동기화 전략
- 알림 수신 즉시 Room에 저장 (오프라인 우선)
- 네트워크 연결 시 서버와 동기화 (`WorkManager` 활용)
- 서버 응답이 최종 정보 (conflict 발생 시 서버 우선)

---

## 6. 네트워크 레이어

```kotlin
// ApiResponse sealed class — 모든 API 응답은 이 타입으로 래핑
sealed class ApiResponse<out T> {
    data class Success<T>(val data: T) : ApiResponse<T>()
    data class Error(val code: Int, val message: String) : ApiResponse<Nothing>()
    object Loading : ApiResponse<Nothing>()
}
```

- 인증 토큰: `AuthInterceptor`에서 헤더 자동 주입
- 토큰 만료: `Authenticator`에서 자동 갱신 처리
- Swagger 기반 API 변경 시 DTO만 수정, 도메인 모델은 mapper에서 흡수

---

## 7. NotificationListenerService 구조

```kotlin
class TransactionNotificationService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val parsed = NotificationParser.parse(sbn) ?: return
        // 파싱 실패 시 조용히 무시
        viewModelScope.launch {
            saveTransactionUseCase(parsed)
        }
    }
}
```

- 파싱 대상 패키지: `docs/references/notification-packages.md` 참고
- 파싱 로직은 `NotificationParser`에 집중, Service는 얇게 유지
