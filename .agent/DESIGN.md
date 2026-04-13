# DESIGN.md

> 앱의 디자인 시스템 및 UI 규칙을 정의합니다.

---

## 1. 디자인 원칙

- **명확함 우선**: 금융 데이터는 읽기 쉽게. 장식보다 가독성.
- **한 화면 한 목적**: 각 화면은 하나의 질문에 답한다.
- **터치 영역**: 모든 인터랙티브 요소는 최소 48dp.

---

## 2. 컬러 시스템

```kotlin
// 지출 = 레드 계열, 수입 = 그린 계열, 중립 = 그레이
object AppColors {
    val Expense = Color(0xFFE53935)       // 지출
    val Income = Color(0xFF43A047)        // 수입
    val Primary = Color(0xFF1976D2)       // 주요 액션
    val Surface = Color(0xFFF5F5F5)       // 배경
    val TextPrimary = Color(0xFF212121)
    val TextSecondary = Color(0xFF757575)
}
```

다크 테마는 v2에서 지원.

---

## 3. 타이포그래피

- 금액 표시: `TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)`
- 가맹점명: `TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium)`
- 날짜/부가정보: `TextStyle(fontSize = 12.sp, color = TextSecondary)`

---

## 4. 공통 컴포넌트

| 컴포넌트 | 위치 | 설명 |
|---------|------|------|
| `TransactionItem` | `presentation/common` | 거래 내역 한 줄 표시 |
| `AmountText` | `presentation/common` | 금액 (색상 자동 분기) |
| `CategoryChip` | `presentation/common` | 카테고리 태그 |
| `LoadingOverlay` | `presentation/common` | 로딩 상태 |
| `ErrorBanner` | `presentation/common` | 에러 배너 |

---

## 5. 네비게이션 구조

```
OnboardingGraph
    └── OnboardingScreen
    └── PermissionScreen

MainGraph (BottomNav)
    ├── HomeScreen
    ├── TransactionListScreen
    │   └── TransactionDetailScreen
    ├── StatisticsScreen
    └── SettingsScreen
```
