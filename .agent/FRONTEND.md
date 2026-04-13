# FRONTEND.md

> Jetpack Compose UI 작성 규칙입니다.
> Codex가 UI 코드를 작성할 때 반드시 따르세요.

---

## 1. Composable 규칙

### Stateless 원칙
```kotlin
// ✅ Good — state와 event를 외부에서 주입
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit
)

// ❌ Bad — ViewModel을 Composable 내부에서 직접 참조
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel())
```

### ViewModel 연결은 Route 레벨에서만
```kotlin
@Composable
fun HomeRoute(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(uiState = uiState, onEvent = viewModel::onEvent)
}
```

---

## 2. 파일 구조 (화면 단위)

```
home/
├── HomeRoute.kt        ← ViewModel 연결
├── HomeScreen.kt       ← Stateless UI
├── HomeViewModel.kt
├── HomeUiState.kt
└── HomeUiEvent.kt
```

---

## 3. LaunchedEffect / SideEffect 규칙

- 일회성 이벤트(토스트, 네비게이션)는 `Channel` + `LaunchedEffect` 조합으로 처리
- `LaunchedEffect(Unit)` 은 초기 데이터 로딩에만 사용
- SideEffect에서 비즈니스 로직 처리 금지

---

## 4. 리스트 성능

- 거래 내역 목록은 반드시 `LazyColumn` 사용
- 아이템에 `key` 파라미터 반드시 지정 (`key = { it.id }`)
- 무거운 Composable은 `remember`로 계산 결과 캐싱

---

## 5. Preview

- 모든 Composable에 `@Preview` 작성
- 로딩/에러/정상 상태 각각 Preview 작성
```kotlin
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        uiState = HomeUiState(transactions = previewTransactions),
        onEvent = {}
    )
}
```
